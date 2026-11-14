// Funções comuns dos efeitos de tela do odteste (#moj_import <odteste:odfx_comum.glsl>).
// Ruído, curvas e a volta da profundidade para posição. Escrito pela mesa Dev.

const float OD_PI = 3.14159265;

float od_suave(float u) {
    u = clamp(u, 0.0, 1.0);
    return u * u * (3.0 - 2.0 * u);
}

// Com coordenada de mundo grande (x 4000) o float perde casas e o hash vira padrão repetido (listras).
// Por isso a entrada volta para 0..289 antes (igual para valores pequenos; o ruído repete a cada 289 células).
float od_hash(vec2 p) {
    p = mod(p, 289.0);
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

// Direção do raio desta coordenada de tela, nos eixos do mundo (sem precisar de profundidade).
vec3 od_raio(mat4 invProj, mat4 invView, vec2 uv) {
    vec4 v = invProj * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
    return normalize((invView * vec4(normalize(v.xyz / v.w), 0.0)).xyz);
}

// Ponto da vista -> relativo à câmera, nos eixos do mundo.
vec3 od_rel(mat4 invView, vec3 vista) {
    return (invView * vec4(vista, 1.0)).xyz;
}

// Normal da superfície pela variação da posição entre pixels vizinhos (virada para a câmera).
vec3 od_normal(vec3 rel) {
    vec3 n = cross(dFdx(rel), dFdy(rel));
    float l = length(n);
    if (l < 1e-9) return vec3(0.0, 1.0, 0.0);
    n /= l;
    if (dot(n, rel) > 0.0) n = -n;
    return n;
}

float od_lum(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

// Ruído com 3 oitavas (mais barato que o fbm, para camadas grandes).
float od_fbm3(vec2 p) {
    float v = 0.5 * od_ruido(p);
    p = mat2(1.6, 1.2, -1.2, 1.6) * p + vec2(3.1, 7.7);
    v += 0.25 * od_ruido(p);
    p = mat2(1.6, 1.2, -1.2, 1.6) * p + vec2(3.1, 7.7);
    v += 0.125 * od_ruido(p);
    return v / 0.875;
}

// Ruído "torcido": o fbm3 lido num ponto que já foi empurrado por outro ruído (fumaça, carne, névoa que rola).
// t = tempo já escalado pelo chamador. Custa dois fbm3 (6 ruídos).
float od_torcido(vec2 p, float t) {
    vec2 q = vec2(od_ruido(p + vec2(0.0, t)), od_ruido(p + vec2(5.2, 1.3 - t * 0.7)));
    return od_fbm3(p + 1.8 * q + vec2(t * 0.35, -t * 0.2));
}

// Sangue como camada que absorve (lei de Beer): fino = mancha a cor de baixo de vermelho (a textura do bloco
// aparece por baixo); grosso = quase preto. Nunca clareia nada.
vec3 od_absorve(vec3 base, float espessura) {
    return base * exp(-espessura * vec3(1.6, 9.0, 8.0));
}

// Garante que o vermelho manda: nenhum realce de sangue vira branco ou rosa.
vec3 od_semBranco(vec3 c) {
    return vec3(c.r, min(c.g, c.r * 0.22), min(c.b, c.r * 0.26));
}

// Pontilhado (interleaved gradient noise): ±meio degrau de 8 bits por pixel. Tira as faixas (banding) dos
// degradês escuros (vinheta vermelha, névoa) quase de graça.
vec3 od_pontilhar(vec3 c, vec2 pixel) {
    float n = fract(52.9829189 * fract(dot(pixel, vec2(0.06711056, 0.00583715))));
    return c + (n - 0.5) / 255.0;
}
