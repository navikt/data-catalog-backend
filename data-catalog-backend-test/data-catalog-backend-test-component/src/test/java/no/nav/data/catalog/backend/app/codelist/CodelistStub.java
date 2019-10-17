package no.nav.data.catalog.backend.app.codelist;

public class CodelistStub {

    public static void initializeCodelist() {
        CodelistCache.init();
        CodelistCache.set(create(ListName.PROVENANCE, "ARBEIDSGIVER", "Arbeidsgiver"));
        CodelistCache.set(create(ListName.PROVENANCE, "SKATTEETATEN", "Skatteetaten"));
        CodelistCache.set(create(ListName.PROVENANCE, "BRUKER", "BRUKER"));
        CodelistCache.set(create(ListName.CATEGORY, "PERSONALIA", "Personalia"));
        CodelistCache.set(create(ListName.CATEGORY, "ARBEIDSFORHOLD", "Arbeidsforhold"));
        CodelistCache.set(create(ListName.CATEGORY, "UTDANNING", "Utdanning"));
        CodelistCache.set(create(ListName.LEGALBASIS, "FTRL", "1997-02-28-19"));
        CodelistCache.set(create(ListName.LEGALBASIS, "NY ALDERSPENSJON", "2009-06-05-32"));
    }

    private static Codelist create(ListName list, String code, String description) {
        return Codelist.builder().list(list).code(code).description(description).build();
    }
}
