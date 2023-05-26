package no.nav.tiltakspenger.mottak.søknad

fun String.erEøs(): Boolean {
    val listeEøsLand = listOf(
        "AT", "AUT",
        "BE", "BEL",
        "BG", "BGR",
        "CH", "CHE",
        "CY", "CYP",
        "CZ", "CZE",
        "DK", "DNK",
        "DE", "DEU",
        "EE", "EST",
        "ES", "ESP",
        "FI", "FIN",
        "FR", "FRA",
        "GR", "GRC",
        "HR", "HRV",
        "HU", "HUN",
        "IE", "IRL",
        "IS", "ISL",
        "IT", "ITA",
        "LI", "LIE",
        "LV", "LVA",
        "LT", "LTU",
        "LU", "LUX",
        "MT", "MLT",
        "NL", "NLD",
        "NO", "NOR",
        "PL", "POL",
        "PT", "PRT",
        "RO", "ROU",
        "SE", "SWE",
        "SI", "SVN",
        "SK", "SVK",
    )
    return listeEøsLand.contains(this)
}

//        Belgia, BE, BEL
//        Bulgaria, BG, BGR
//        Danmark, DK, DNK
//        Estland, EE, EST
//        Finland, FI, FIN
//        Frankrike, FR, FRA
//        Hellas, GR, GRC
//        Irland, IE, IRL
//        Island, IS, ISL
//        Italia, IT ITA
//        Kroatia, HR, HRV
//        Kypros, CY, CYP
//        Latvia, LV, LVA
//        Liechtenstein, LI, LIE
//        Litauen, LT, LTU
//        Luxembourg, LU, LUX
//        Malta, MT, MLT
//        Nederland, NL, NLD
//        Norge, NO, NOR
//        Polen, PL, POL
//        Portugal, PT, PRT
//        Romania, RO, ROU
//        Slovakia, SK, SVK
//        Slovenia, SI, SVN
//        Spania, ES, ESP
//        Sveits, CH, CHE (Sveits er ikke EØS-land, men omfattes av reglene for koordinering av trygd)
//        Sverige, SE, SWE
//        Tsjekkia, CZ, CZE
//        Tyskland, DE, DEU
//        Ungarn, HU, HUN
//        Østerrike, AT, AUT
