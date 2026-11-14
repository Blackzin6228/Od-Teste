// Funções comuns dos efeitos de tela do odteste (#moj_import <odteste:odfx_comum.glsl>).
// Ruído, curvas e a volta da profundidade para posição. Escrito pela mesa Dev.

const float OD_PI = 3.14159265;

float od_suave(float u) {
    u = clamp(u, 0.0, 1.0);
    return u * u * (3.0 - 2.0 * u);
}

float od_hash(vec2 p) {
    p = fract(p * vec2(443.897, 441.423));
    p += dot(p, p.yx + 19.19);
    return fract((p.x + p.y) * p.x);
}

float od_ruido(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = od_hash(i);
    float b = od_hash(i + vec2(1.0, 0.0));
    float c = od_hash(i + vec2(0.0, 1.0));
    float d = od_hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float od_fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * od_ruido(p);
        p = mat2(1.6, 1.2, -1.2, 1.6) * p + vec2(3.1, 7.7);
        a *= 0.5;
    }
    return v;
}

// Linha fina com borda suave (largura em unidades da própria coordenada).
float od_linha(float d, float largura) {
    float w = max(fwidth(d), 1e-5);
    return 1.0 - smoothstep(largura, largura + w * 1.5, abs(d));
}

// Ponto da cena nesta coordenada de tela, relativo à câmera, nos eixos da vista.
// w = 1 para terreno e coisas; w = 0 para o céu (profundidade 1).
vec4 od_vista(sampler2D profundidade, mat4 invProj, vec2 uv) {
    float z = texture(profundidade, uv).r;
    vec4 clip = vec4(uv * 2.0 - 1.0, z * 2.0 - 1.0, 1.0);
    vec4 v = invProj * clip;
    return vec4(v.xyz / v.w, z < 0.99999 ? 1.0 : 0.0);
}

vec3 od_cinza(vec3 c) {
    return vec3(dot(c, vec3(0.299, 0.587, 0.114)));
}
