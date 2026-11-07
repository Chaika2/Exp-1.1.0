#version 330 core
in vec2 uv; out vec4 outColor;
uniform sampler2D BeforeTexture,AfterTexture,DepthBeforeTexture,DepthAfterTexture;
uniform vec2 resolution;
float dm(vec2 p){
 float db=texture(DepthBeforeTexture,p).r,da=texture(DepthAfterTexture,p).r;
 return smoothstep(0.000015,0.00016,db-da);
}
void main(){
 vec2 px=1.0/max(resolution,vec2(1.0));
 float hand=dm(uv);
 hand=max(hand,dm(clamp(uv+vec2(px.x,0.0),vec2(0.0),vec2(1.0)))*0.88);
 hand=max(hand,dm(clamp(uv-vec2(px.x,0.0),vec2(0.0),vec2(1.0)))*0.88);
 hand=max(hand,dm(clamp(uv+vec2(0.0,px.y),vec2(0.0),vec2(1.0)))*0.88);
 hand=max(hand,dm(clamp(uv-vec2(0.0,px.y),vec2(0.0),vec2(1.0)))*0.88);
 outColor=vec4(hand,hand,hand,1.0);
}
