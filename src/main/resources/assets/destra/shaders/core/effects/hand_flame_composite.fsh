#version 330 core
in vec2 uv; out vec4 outColor;
uniform sampler2D MaskTexture,GlowTexture,TrailTexture;
uniform vec2 resolution; uniform float time,fillMode,radiusPx,strength,flameSpeed,colorMix,decay,flameHeightPx,trailStrength; uniform vec3 fireColor;
float sm(vec2 p){return clamp(texture(MaskTexture,p).r,0.,1.);}
void main(){vec2 px=1./resolution;vec4 gl=texture(GlowTexture,uv),tr=texture(TrailTexture,uv);float hm=sm(uv),outside=1.-smoothstep(.015,.20,hm),gate=mix(outside,1.,clamp(fillMode,0.,1.));float n=0.;n=max(n,sm(uv+vec2(px.x,0)));n=max(n,sm(uv-vec2(px.x,0)));n=max(n,sm(uv+vec2(0,px.y)));n=max(n,sm(uv-vec2(0,px.y)));float edge=clamp(max(gl.a*1.12,n)*gate,0.,1.);vec3 add=tr.rgb*clamp(.88+edge*.32,0.,1.)*gate*trailStrength*.86;float ga=clamp(gl.a*(.94+edge*.32)*gate,0.,.76);add+=gl.rgb*ga*(.72+strength*.26);outColor=vec4(clamp(add,0.,1.),1.);}
