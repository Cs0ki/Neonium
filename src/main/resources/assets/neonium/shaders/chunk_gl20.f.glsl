#version 110

varying vec4 v_Color; // The interpolated vertex color
varying vec2 v_TexCoord; // The interpolated block texture coordinates
varying vec2 v_LightCoord; // The interpolated light map texture coordinates

uniform sampler2D u_BlockTex; // The block texture sampler
uniform sampler2D u_LightTex; // The light map texture sampler

#ifdef USE_FOG
varying float v_FragDistance;
uniform vec4 u_FogColor; // The color of the fog
#endif

#ifdef USE_FOG_EXP2
uniform float u_FogDensity;

// e^(-density * c^2)
float getFogFactor() {
    float dist = v_FragDistance * u_FogDensity;
    
    // Improved water fog calculation to match vanilla behavior
    // Vanilla water has a higher visibility range, especially for nearby blocks
    // This creates a more gradual fog effect in water
    bool isWaterFog = u_FogColor.b > 0.2 && u_FogColor.r < 0.2 && u_FogColor.g < 0.4;
    if (isWaterFog) {
        // Scale the distance to match vanilla water visibility
        // This makes water less dark when close but maintains fog for distance
        dist *= 0.5; // Reduced density for water to see farther
        
        // Apply a more linear falloff for water - based on vanilla behavior
        if (dist < 1.5) {
            return 1.0 - (dist * 0.25); // More transparent for close blocks
        }
    }
    
    return 1.0 / exp2(dist * dist);
}
#endif

#ifdef USE_FOG_LINEAR
uniform float u_FogLength; // FogStart - FogEnd
uniform float u_FogEnd;

// (end - dist) / (end - start)
float getFogFactor() {
    float dist = v_FragDistance;
    
    // Improved water fog calculation to match vanilla behavior
    bool isWaterFog = u_FogColor.b > 0.2 && u_FogColor.r < 0.2 && u_FogColor.g < 0.4;
    if (isWaterFog) {
        // Increase the fog end distance for water to match vanilla
        return clamp((u_FogEnd * 1.8 - dist) / (u_FogLength * 1.8), 0.0, 1.0);
    }
    
    return (u_FogEnd - dist) / u_FogLength;
}
#endif

void main() {
    // Block texture sample
    vec4 sampleBlockTex = texture2D(u_BlockTex, v_TexCoord);

    // Light map texture sample
    vec4 sampleLightTex = texture2D(u_LightTex, v_LightCoord);

    // Blend the colors from both textures and the vertex itself
    vec4 diffuseColor = sampleBlockTex * sampleLightTex * v_Color;

#ifdef USE_FOG
    float fogFactor = clamp(getFogFactor(), 0.0, 1.0);

    gl_FragColor = mix(u_FogColor, diffuseColor, fogFactor);
    gl_FragColor.a = diffuseColor.a;
#else
    // No fog is being used, so the fragment color is just that of the blended texture color
    gl_FragColor = diffuseColor;
#endif
}
