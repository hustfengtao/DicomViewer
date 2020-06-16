package com.taofe.dicomviewer;

import android.opengl.GLES20;
import java.nio.FloatBuffer;
import java.util.ArrayList;



public class Slice {
    private FloatBuffer vertexBuffer;
    private float pointCoor[];
    private float color[] = {255, 255, 255, 1};
    private short[] index;
    private ArrayList<Point3D> list = new ArrayList<Point3D>();
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMatrixHandle;
    private final int COORDS_PER_VERTEX = 3;
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    private class Point3D{
        float x;
        float y;
        float z;
        Point3D(float x, float y, float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    public Slice(short[] data, int width, int height, float z, float rate){
        for (int i=0;i!=width;i++){
            for (int j=0;j!=height;j++){
                if (data[j*width + i] == 0){
                    continue;
                }else{
                    list.add(new Point3D((float)(i - width/2)/rate, (float)(height/2 - j)/rate, z/rate));
                }
            }
        }
        pointCoor = new float[list.size()*3];
        for (int k=0;k!=list.size()*3-1;k++){
            if ( k % 3 == 0){
                pointCoor[k] = list.get(k/3).x;
            }else if(k % 3 == 1){
                pointCoor[k] = list.get(k/3).y;
            }else if(k % 3 == 2){
                pointCoor[k] = list.get(k/3).z;
            }
        }
        index = new short[list.size()];
        for (int m = 0;m!=list.size();m++){
            index[m] = (short)m;
        }
        vertexBuffer = BufferTransformUtil.floatBufferUtil(pointCoor);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // 创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();

        // 添加顶点着色器到程序中
        GLES20.glAttachShader(mProgram, vertexShader);

        // 添加片段着色器到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);

        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);

    }

    public void draw(float[] mvpMatrix){
        int vertexCount = pointCoor.length / COORDS_PER_VERTEX;
        int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

        GLES20.glUseProgram(mProgram);
        // 获取顶点着色器的位置的句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用顶点位置的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // 获取片段着色器的颜色的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 设置绘制的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mvpMatrix, 0);

        // 绘制
        GLES20.glLineWidth(1);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }


    public static int loadShader(int type, String shaderCode){

        // 创造顶点着色器类型(GLES20.GL_VERTEX_SHADER)
        // 或者是片段着色器类型 (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // 添加上面编写的着色器代码并编译它
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
