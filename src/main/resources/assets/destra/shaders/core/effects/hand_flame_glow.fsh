#version 330 core
in vec2 uv; out vec4 outColor;
uniform sampler2D HandTexture,MaskTexture;
uniform vec2 resolution; uniform float time,fillMode,radiusPx,strength,flameSpeed,colorMix,decay,flameHeightPx,trailStrength; uniform vec3 fireColor;
const int RINGS=6, DIRS=12; const float TAU=6.28318530718;
float hash12(vec2 p){vec3 p3=fract(vec3(p.xyx)*.1031);p3+=dot(p3,p3.yzx+33.33);return fract((p3.x+p3.y)*p3.z);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p),u=f*f*(3.-2.*f);return mix(mix(hash12(i),hash12(i+vec2(1,0)),u.x),mix(hash12(i+vec2(0,1)),hash12(i+1.),u.x),u.y);}
vec3 itemColor(vec3 c){c=clamp(c,0.,1.);float mx=max(max(c.r,c.g),c.b);if(mx<.04)return vec3(0);float g=dot(c,vec3(.299,.587,.114));float s=mx-min(min(c.r,c.g),c.b);vec3 v=mix(vec3(g),c,1.+s*.45);return clamp(v/max(max(max(v.r,v.g),v.b),.20),0.,1.)*min(mx*1.18,1.);}
void main(){vec2 px=1./resolution;float center=texture(MaskTexture,uv).r;float outside=1.-smoothstep(.015,.22,center),flow=time*flameSpeed,energy=0.,cw=0.;vec3 sum=vec3(0);
 for(int r=1;r<=RINGS;r++){float rp=float(r)/float(RINGS),dist=radiusPx*rp,rw=pow(1.-rp*.86,1.45);for(int d=0;d<DIRS;d++){float w=noise(uv*9.+vec2(float(r)*3.4,float(d)*2.2+flow*.16));float a=float(d)/float(DIRS)*TAU+(w-.5)*.34;vec2 q=uv+vec2(cos(a),sin(a))*px*dist*(.9+w*.22);float ma=clamp(texture(MaskTexture,q).r,0.,1.),wt=ma*rw;energy+=wt;sum+=texture(HandTexture,q).rgb*wt;cw+=wt;}}
 energy=energy/float(RINGS*DIRS)*4.25;float halo=smoothstep(.018,.58,energy),n=noise(uv*vec2(15,21)+vec2(flow*.2,-flow*.34));float a=clamp(halo*outside*strength*(.86+n*.18),0.,.72);vec3 c=mix(itemColor(sum/max(cw,.001)),fireColor,clamp(colorMix,0.,1.));outColor=vec4(c*(.88+a*.42),a);}
