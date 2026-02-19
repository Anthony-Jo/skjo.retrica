#version 100

#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 v_TexCoord; // Vertex Shader로부터 받은 텍스처 좌표

// 카메라 프리뷰가 담길 텍스처 샘플러
uniform samplerExternalOES u_Texture;

void main()
{
    gl_FragColor = texture2D(u_Texture, v_TexCoord);
}
