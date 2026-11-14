#version 150

// Cor do horror (overworld inteiro, de dia também). Escrito do zero pela mesa Dev.
//
// O mundo fica lavado e frio, como um dia nublado que não termina: pouca cor (o que é colorido continua
// reconhecível, só sem vida), sombras leitosas de contraste baixo, céu puxado para cinza, um ar frio que engole
// a distância, grão leve e cantos escuros. À noite e nas áreas da história fica mais pesado. Não mexe na luz:
// quem escurece a noite é a camada de horror (mapa de luz); aqui é só a cor.
// Sem profundidade (shader pack que não deixa ler): o mesmo tom, com o céu achado pela direção e pela cor.

uniform sampler2D Sampler0;      // cor da cena
uniform sampler2D Sampler1;      // profundidade da cena
uniform mat4 InvProj;
uniform mat4 InvView;
uniform vec2 ScreenSize;
uniform float Tempo;             // segundos
uniform float Forca;             // 0..1 (overworld, efeitos_fortes, dá lugar ao carmesim)
uniform float ComProfundidade;   // 1 = profundidade boa; 0 = versão sem profundidade
uniform float Noite;             // 0..1
uniform float Area;              // 0..1 dentro de uma área da história

in vec2 texCoord;
out vec4 fragColor;

#moj_import <odteste:odfx_comum.glsl>

// Ar frio: cinza azulado de dia, quase preto de noite.
vec3 corDoAr(float noite) {
    return mix(vec3(0.40, 0.43, 0.46), vec3(0.030, 0.034, 0.042), noite);
}

// O tom do mundo: menos cor, frio, sombra leitosa, luz contida.
vec3 graduar(vec3 c, float p, float noite) {
    float l = od_lum(c);
    // 1) cor: de dia sobra ~45% da saturação, de noite ~25%
    float sat = mix(0.34, 0.22, noite);
    // luz que se acende (tocha, lanterna, fogo, painel de máquina) guarda a cor: o calor no meio do frio
    float viva = max(max(c.r, c.g), c.b);
    float acesa = smoothstep(mix(0.30, 0.16, noite), mix(0.65, 0.45, noite), viva) * smoothstep(0.25, 0.55, (viva - min(min(c.r, c.g), c.b)) / max(viva, 0.01));
    sat = mix(sat, 0.92, acesa * mix(0.35, 1.0, noite));
    vec3 r = mix(vec3(l), c, mix(1.0, sat, p));
    // 2) frio: sombras puxam para azul-esverdeado, meios para cinza azulado
    vec3 frioSombra = vec3(0.86, 0.96, 1.08);
    vec3 frioLuz = vec3(0.95, 0.98, 1.02);
    r *= mix(vec3(1.0), mix(frioSombra, frioLuz, smoothstep(0.05, 0.6, l)), p * (1.0 - acesa * 0.9));
    // 3) contraste baixo nas sombras: o preto vira um cinza frio (de noite bem pouco, a noite é escura)
    float ergue = mix(0.060, 0.012, noite) * p;
    r = vec3(0.80, 0.92, 1.05) * ergue + r * (1.0 - ergue);
    // 4) luz contida: o branco não estoura e o dia fica um pouco mais baixo
    float topo = smoothstep(0.45, 1.0, l);
    r *= 1.0 - p * (0.20 + 0.12 * topo);
    return r;
}

// Céu: o azul vira cinza de nuvem carregada (o brilho do sol e das nuvens fica, só sem cor).
vec3 ceuCinza(vec3 c, float p, float noite) {
    float l = od_lum(c);
    vec3 cinza = vec3(l * 0.62 + 0.08) * vec3(0.95, 0.98, 1.02);
    cinza = mix(cinza, vec3(l * 0.9) * vec3(0.90, 0.95, 1.05), noite);
    return mix(c, cinza, p * 0.85);
}

// Grão de filme e cantos escuros.
vec3 acabamento(vec3 c, vec2 uv, float p, float noite) {
    float g = od_hash(uv * ScreenSize + fract(Tempo * 7.0) * 113.0) - 0.5;
    c += g * mix(0.030, 0.040, noite) * p * (0.35 + od_lum(c));
    vec2 d = uv - 0.5;
    c *= 1.0 - p * mix(0.34, 0.42, noite) * od_suave(dot(d, d) * 2.4);
    return max(c, 0.0);
}

void main() {
    vec2 uv = texCoord;
    vec3 c = texture(Sampler0, uv).rgb;
    float k = clamp(Forca, 0.0, 1.0);
    if (k < 0.002) {
        fragColor = vec4(c, 1.0);
        return;
    }
    float noite = clamp(Noite, 0.0, 1.0);
    // de dia já é forte; noite e área da história levam ao máximo
    float p = k * mix(0.78, 1.0, max(noite, clamp(Area, 0.0, 1.0)));
    vec3 rd = od_raio(InvProj, InvView, uv);

    if (ComProfundidade > 0.5) {
        vec4 v = od_vista(Sampler1, InvProj, uv);
        if (v.w < 0.5) {
            vec3 r = ceuCinza(c, p, noite);
            r = graduar(r, p * 0.6, noite);
            fragColor = vec4(acabamento(r, uv, p, noite), 1.0);
            return;
        }
        float dist = length(od_rel(InvView, v.xyz));
        vec3 r = graduar(c, p, noite);
        // ar frio que engole a distância (mais curto de noite e nas áreas)
        float alcance = mix(0.016, 0.022, max(noite, Area * 0.6));
        float ar = (1.0 - exp(-dist * alcance)) * mix(0.65, 0.75, noite) * p;
        // o ar fica no tom do próprio céu ali (a névoa do jogo já existe: aqui só esfria e acinzenta)
        r = mix(r, corDoAr(noite), ar);
        fragColor = vec4(acabamento(r, uv, p, noite), 1.0);
        return;
    }

    // ---- sem profundidade: céu pela direção + cor (azul claro olhando para cima), ar pela altura do olhar
    float l = od_lum(c);
    float azul = smoothstep(0.02, 0.14, c.b - max(c.r, c.g * 0.92));
    float escuroNoAlto = smoothstep(0.10, 0.03, l) * noite;
    float ceu = smoothstep(0.0, 0.12, rd.y) * clamp(max(azul, escuroNoAlto) + smoothstep(0.75, 0.95, l) * 0.6, 0.0, 1.0);
    vec3 r = mix(graduar(c, p, noite), graduar(ceuCinza(c, p, noite), p * 0.6, noite), ceu);
    float horizonte = exp(-abs(rd.y) * 7.0) * (1.0 - ceu);
    r = mix(r, corDoAr(noite), horizonte * 0.28 * p);
    fragColor = vec4(acabamento(r, uv, p, noite), 1.0);
}
