#version 100

attribute vec4 a_Position; // 정점의 위치 정보
attribute vec2 a_TexCoord; // 정점에 매핑될 텍스처의 좌표 정보

varying vec2 v_TexCoord; // Fragment Shader로 전달할 텍스처 좌표

void main()
{
    // 정점의 위치를 그대로 사용
    gl_Position = a_Position;

    // 텍스처 좌표를 Fragment Shader로 전달
    v_TexCoord = a_TexCoord;
}
