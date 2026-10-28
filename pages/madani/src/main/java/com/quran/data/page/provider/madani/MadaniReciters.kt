package com.quran.data.page.provider.madani

import com.quran.data.model.audio.Qari
import com.quran.labs.androidquran.common.audio.R

/** Reciter IDs, audio formats and timing databases verified against the supplied 3.6.4 APK. */
object MadaniReciters {
  val all: List<Qari> = listOf(
    Qari(
      id = 0, nameResource = R.string.qari_minshawi_murattal_gapless,
      url = "https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshaawee/",
      opusUrl = "https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshaawee/opus/", path = "minshawi_murattal",
      hasGaplessAlternative = false, db = "minshawi_murattal"
    ),
    Qari(
      id = 1, nameResource = R.string.qari_husary_gapless,
      url = "https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree/",
      opusUrl = "https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree/opus/", path = "husary",
      hasGaplessAlternative = false, db = "husary"
    ),
    Qari(
      id = 2, nameResource = R.string.qari_sudais_gapless,
      url = "https://download.quranicaudio.com/quran/abdurrahmaan_as-sudays/",
      opusUrl = "https://download.quranicaudio.com/quran/abdurrahmaan_as-sudays/opus/", path = "sudais",
      hasGaplessAlternative = false, db = "sudais_murattal"
    ),
    Qari(
      id = 3, nameResource = R.string.qari_qatami_gapless,
      url = "https://download.quranicaudio.com/quran/nasser_bin_ali_alqatami/",
      opusUrl = "https://download.quranicaudio.com/quran/nasser_bin_ali_alqatami/opus/", path = "qatami",
      hasGaplessAlternative = false, db = "qatami"
    ),
    Qari(
      id = 4, nameResource = R.string.qari_abdulbaset,
      url = "https://mirrors.quranicaudio.com/everyayah/Abdul_Basit_Murattal_192kbps/",
      opusUrl = null, path = "0",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 5, nameResource = R.string.qari_abdulbaset_mujawwad,
      url = "https://mirrors.quranicaudio.com/everyayah/Abdul_Basit_Mujawwad_128kbps/",
      opusUrl = null, path = "1",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 6, nameResource = R.string.qari_basfar,
      url = "https://mirrors.quranicaudio.com/everyayah/Abdullah_Basfar_192kbps/",
      opusUrl = null, path = "2",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 7, nameResource = R.string.qari_suadis,
      url = "https://mirrors.quranicaudio.com/everyayah/Abdurrahmaan_As-Sudais_192kbps/",
      opusUrl = null, path = "3",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 8, nameResource = R.string.qari_shatri,
      url = "https://mirrors.quranicaudio.com/everyayah/Abu_Bakr_Ash-Shaatree_128kbps/",
      opusUrl = null, path = "4",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 9, nameResource = R.string.qari_afasy,
      url = "https://mirrors.quranicaudio.com/everyayah/Alafasy_128kbps/",
      opusUrl = null, path = "5",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 10, nameResource = R.string.qari_saad_al_ghamdi,
      url = "https://mirrors.quranicaudio.com/everyayah/Ghamadi_40kbps/",
      opusUrl = null, path = "18",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 11, nameResource = R.string.qari_walk,
      url = "https://mirrors.quranicaudio.com/everyayah/Ibrahim_Walk_192kbps_TEST/",
      opusUrl = null, path = "19",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 12, nameResource = R.string.qari_hani_rifai,
      url = "https://mirrors.quranicaudio.com/everyayah/Hani_Rifai_192kbps/",
      opusUrl = null, path = "6",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 13, nameResource = R.string.qari_husary_mujawwad,
      url = "https://mirrors.quranicaudio.com/everyayah/Husary_128kbps_Mujawwad/",
      opusUrl = null, path = "8",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 14, nameResource = R.string.qari_hudhayfi,
      url = "https://mirrors.quranicaudio.com/everyayah/Hudhaify_128kbps/",
      opusUrl = null, path = "9",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 15, nameResource = R.string.qari_muaiqly,
      url = "https://mirrors.quranicaudio.com/everyayah/Maher_AlMuaiqly_64kbps/",
      opusUrl = null, path = "11",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 16, nameResource = R.string.qari_minshawi_mujawwad,
      url = "https://mirrors.quranicaudio.com/everyayah/Minshawy_Mujawwad_192kbps/",
      opusUrl = null, path = "13",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 17, nameResource = R.string.qari_tablawy,
      url = "https://mirrors.quranicaudio.com/everyayah/Mohammad_al_Tablaway_128kbps/",
      opusUrl = null, path = "14",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 18, nameResource = R.string.qari_ayyoub,
      url = "https://mirrors.quranicaudio.com/everyayah/Muhammad_Ayyoub_128kbps/",
      opusUrl = null, path = "15",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 19, nameResource = R.string.qari_jibreel,
      url = "https://mirrors.quranicaudio.com/everyayah/Muhammad_Jibreel_128kbps/",
      opusUrl = null, path = "16",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 20, nameResource = R.string.qari_shuraym,
      url = "https://mirrors.quranicaudio.com/everyayah/Saood_ash-Shuraym_128kbps/",
      opusUrl = null, path = "17",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 21, nameResource = R.string.qari_dussary,
      url = "https://mirrors.quranicaudio.com/everyayah/Yasser_Ad-Dussary_128kbps/",
      opusUrl = null, path = "ydussary",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 22, nameResource = R.string.qari_abdulbaset_gapless,
      url = "https://download.quranicaudio.com/quran/abdul_basit_murattal/",
      opusUrl = "https://download.quranicaudio.com/quran/abdul_basit_murattal/opus/", path = "abdulbaset_murattal",
      hasGaplessAlternative = false, db = "abdul_basit_murattal"
    ),
    Qari(
      id = 23, nameResource = R.string.qari_abdulbaset_mujawwad_gapless,
      url = "https://download.quranicaudio.com/quran/abdulbaset_mujawwad/",
      opusUrl = "https://download.quranicaudio.com/quran/abdulbaset_mujawwad/opus/", path = "abdulbaset_mujawwad",
      hasGaplessAlternative = false, db = "abdul_basit_mujawwad"
    ),
    Qari(
      id = 24, nameResource = R.string.qari_aziz_alili_gapless,
      url = "https://download.quranicaudio.com/quran/aziz_alili/",
      opusUrl = "https://download.quranicaudio.com/quran/aziz_alili/opus/", path = "aziz_alili",
      hasGaplessAlternative = false, db = "aziz_alili"
    ),
    Qari(
      id = 25, nameResource = R.string.qari_salah_budair_gapless,
      url = "https://download.quranicaudio.com/quran/salahbudair/",
      opusUrl = "https://download.quranicaudio.com/quran/salahbudair/opus/", path = "salah_budair",
      hasGaplessAlternative = false, db = "salah_budair"
    ),
    Qari(
      id = 26, nameResource = R.string.qari_shuraym_gapless,
      url = "https://download.quranicaudio.com/quran/sa3ood_al-shuraym/",
      opusUrl = "https://download.quranicaudio.com/quran/sa3ood_al-shuraym/opus/", path = "shuraym",
      hasGaplessAlternative = false, db = "shuraym"
    ),
    Qari(
      id = 27, nameResource = R.string.qari_yasser_dussary_gapless,
      url = "https://download.quranicaudio.com/quran/yasser_ad-dussary/",
      opusUrl = "https://download.quranicaudio.com/quran/yasser_ad-dussary/opus/", path = "yasser_dussary",
      hasGaplessAlternative = false, db = "yasser_dussary"
    ),
    Qari(
      id = 28, nameResource = R.string.qari_mohammad_altablawi_gapless,
      url = "https://download.quranicaudio.com/quran/mohammad_altablawi/",
      opusUrl = "https://download.quranicaudio.com/quran/mohammad_altablawi/opus/", path = "mohammad_altablawi",
      hasGaplessAlternative = false, db = "mohammad_altablawi"
    ),
    Qari(
      id = 29, nameResource = R.string.qari_sahl_yaseen_gapless,
      url = "https://download.quranicaudio.com/quran/sahl_yaaseen/",
      opusUrl = "https://download.quranicaudio.com/quran/sahl_yaaseen/opus/", path = "sahl_yaseen",
      hasGaplessAlternative = false, db = "sahl_yaseen"
    ),
    Qari(
      id = 30, nameResource = R.string.qari_shatri_gapless,
      url = "https://download.quranicaudio.com/quran/abu_bakr_ash-shaatree/",
      opusUrl = "https://download.quranicaudio.com/quran/abu_bakr_ash-shaatree/opus/", path = "shatri",
      hasGaplessAlternative = false, db = "shatri"
    ),
    Qari(
      id = 31, nameResource = R.string.qari_ahmad_nauina_gapless,
      url = "https://download.quranicaudio.com/quran/ahmad_nauina/",
      opusUrl = "https://download.quranicaudio.com/quran/ahmad_nauina/opus/", path = "ahmad_nauina",
      hasGaplessAlternative = false, db = "ahmad_nauina"
    ),
    Qari(
      id = 32, nameResource = R.string.qari_akram_al_alaqmi,
      url = "https://download.quranicaudio.com/quran/akram_al_alaqmi/",
      opusUrl = "https://download.quranicaudio.com/quran/akram_al_alaqmi/opus/", path = "akram_al_alaqmi",
      hasGaplessAlternative = false, db = "akram_al_alaqmi"
    ),
    Qari(
      id = 33, nameResource = R.string.qari_ali_hajjaj_alsouasi_gapless,
      url = "https://download.quranicaudio.com/quran/ali_hajjaj_alsouasi/",
      opusUrl = "https://download.quranicaudio.com/quran/ali_hajjaj_alsouasi/opus/", path = "ali_hajjaj_alsouasi",
      hasGaplessAlternative = false, db = "ali_hajjaj_alsouasi"
    ),
    Qari(
      id = 34, nameResource = R.string.qari_saad_al_ghamidi_gapless,
      url = "https://download.quranicaudio.com/quran/sa3d_al-ghaamidi/complete/128kbps/",
      opusUrl = null, path = "sa3d_alghamidi",
      hasGaplessAlternative = false, db = "sa3d_alghamidi"
    ),
    Qari(
      id = 35, nameResource = R.string.qari_bandar_baleela_gapless,
      url = "https://download.quranicaudio.com/quran/bandar_baleela/complete/",
      opusUrl = "https://download.quranicaudio.com/quran/bandar_baleela/complete/opus/", path = "bandar_baleela",
      hasGaplessAlternative = false, db = "bandar_baleela"
    ),
    Qari(
      id = 36, nameResource = R.string.qari_mahmoud_ali_albana_gapless,
      url = "https://download.quranicaudio.com/quran/mahmood_ali_albana/",
      opusUrl = "https://download.quranicaudio.com/quran/mahmood_ali_albana/opus/", path = "mahmoud_ali_albana",
      hasGaplessAlternative = false, db = "mahmoud_ali_albana"
    ),
    Qari(
      id = 37, nameResource = R.string.qari_abdulrahman_alshahat_gapless,
      url = "https://download.quranicaudio.com/quran/abdulrahman_al_shahat/",
      opusUrl = "https://download.quranicaudio.com/quran/abdulrahman_al_shahat/opus/", path = "abdurlrahman_alshahat",
      hasGaplessAlternative = false, db = "abdulrahman_alshahat"
    ),
    Qari(
      id = 38, nameResource = R.string.qari_abdurrashid_sufi_gapless,
      url = "https://download.quranicaudio.com/quran/abdurrashid_sufi/128kbps/",
      opusUrl = null, path = "abdurrashid_sufi",
      hasGaplessAlternative = false, db = "abdurrashid_sufi"
    ),
    Qari(
      id = 39, nameResource = R.string.qari_mostafa_ismaeel_gapless,
      url = "https://download.quranicaudio.com/quran/mostafa_ismaeel/128kbps/",
      opusUrl = null, path = "mostafa_ismaeel",
      hasGaplessAlternative = false, db = "mostafa_ismaeel"
    ),
    Qari(
      id = 40, nameResource = R.string.qari_husary_iza3a_gapless,
      url = "https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree_iza3a/128kbps/",
      opusUrl = null, path = "husary_iza3a",
      hasGaplessAlternative = false, db = "husary_iza3a"
    ),
    Qari(
      id = 41, nameResource = R.string.qari_abdulaziz_zahrani_gapless,
      url = "https://download.quranicaudio.com/quran/abdulaziz_bin_saleh_alzahrani/",
      opusUrl = "https://download.quranicaudio.com/quran/abdulaziz_bin_saleh_alzahrani/opus/", path = "abdulaziz_zahrani",
      hasGaplessAlternative = false, db = "abdulaziz_zahrani"
    ),
    Qari(
      id = 42, nameResource = R.string.qari_ayman_suwaid_gapped,
      url = "https://mirrors.quranicaudio.com/everyayah/Ayman_Sowaid_64kbps/",
      opusUrl = null, path = "ayman_suwaid",
      hasGaplessAlternative = true, db = null
    ),
    Qari(
      id = 43, nameResource = R.string.qari_afasy_gapless,
      url = "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/",
      opusUrl = "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/opus/", path = "mishari_alafasy",
      hasGaplessAlternative = false, db = "mishari_alafasy"
    ),
    Qari(
      id = 44, nameResource = R.string.qari_hani_rifai_gapless,
      url = "https://download.quranicaudio.com/quran/rifai/",
      opusUrl = "https://download.quranicaudio.com/quran/rifai/opus/", path = "hani_rifai",
      hasGaplessAlternative = false, db = "hani_rifai"
    ),
    Qari(
      id = 45, nameResource = R.string.qari_ayyoub_gapless,
      url = "https://download.quranicaudio.com/quran/muhammad_ayyoob/",
      opusUrl = "https://download.quranicaudio.com/quran/muhammad_ayyoob/opus/", path = "muhammad_ayyoub",
      hasGaplessAlternative = false, db = "muhammad_ayyoub"
    ),
    Qari(
      id = 46, nameResource = R.string.qari_mishari_walk_gapless,
      url = "https://download.quranicaudio.com/quran/mishaari_w_ibrahim_walk_si/",
      opusUrl = "https://download.quranicaudio.com/quran/mishaari_w_ibrahim_walk_si/opus/", path = "mishari_walk",
      hasGaplessAlternative = false, db = "mishari_walk"
    ),
    Qari(
      id = 47, nameResource = R.string.qari_jibreel_gapless,
      url = "https://download.quranicaudio.com/quran/muhammad_jibreel/complete/reencoded/",
      opusUrl = null, path = "mjibreel",
      hasGaplessAlternative = false, db = "mjibreel"
    ),
    Qari(
      id = 48, nameResource = R.string.qari_basfar_gapless,
      url = "https://download.quranicaudio.com/quran/abdullaah_basfar/archive/",
      opusUrl = null, path = "abdullah_basfar",
      hasGaplessAlternative = false, db = "abdullah_basfar"
    ),
    Qari(
      id = 49, nameResource = R.string.qari_walk_gapless,
      url = "https://download.quranicaudio.com/quran/ibrahim_walk/",
      opusUrl = "https://download.quranicaudio.com/quran/ibrahim_walk/opus/", path = "ibrahim_walk",
      hasGaplessAlternative = false, db = "ibrahim_walk"
    ),
    Qari(
      id = 50, nameResource = R.string.qari_afasy_cali_gapless,
      url = "https://download.quranicaudio.com/quran/mishaari_california/",
      opusUrl = "https://download.quranicaudio.com/quran/mishaari_california/opus/", path = "mishari_cali",
      hasGaplessAlternative = false, db = "mishari_alafasy_cali"
    ),
    Qari(
      id = 51, nameResource = R.string.qari_ajamy_gapless,
      url = "https://download.quranicaudio.com/quran/ahmed_ibn_3ali_al-3ajamy/",
      opusUrl = "https://download.quranicaudio.com/quran/ahmed_ibn_3ali_al-3ajamy/opus/", path = "ahmed_alajamy",
      hasGaplessAlternative = false, db = "ahmed_al3ajamy"
    ),
    Qari(
      id = 52, nameResource = R.string.qari_ali_jaber_gapless,
      url = "https://download.quranicaudio.com/quran/ali_jaber/",
      opusUrl = "https://download.quranicaudio.com/quran/ali_jaber/opus/", path = "ali_jaber",
      hasGaplessAlternative = false, db = "ali_jaber"
    ),
    Qari(
      id = 53, nameResource = R.string.qari_muaiqly_haramain_gapless,
      url = "https://mirrors.quranicaudio.com/tvquran/maher_al_mu3aiqly/",
      opusUrl = null, path = "maher_al_muaiqly",
      hasGaplessAlternative = false, db = "maher_al_muaiqly"
    ),
    Qari(
      id = 54, nameResource = R.string.qari_juhany_gapless,
      url = "https://download.quranicaudio.com/quran/abdullaah_3awwaad_al-juhaynee/mobile/",
      opusUrl = null, path = "abdullah_juhany",
      hasGaplessAlternative = false, db = "abdullah_juhany"
    ),
    Qari(
      id = 55, nameResource = R.string.qari_abdulmuhsin_qasim_gapless,
      url = "https://download.quranicaudio.com/quran/abdul_muhsin_alqasim/",
      opusUrl = "https://download.quranicaudio.com/quran/abdul_muhsin_alqasim/opus/", path = "abdul_muhsin_alqasim",
      hasGaplessAlternative = false, db = "abdul_muhsin_alqasim"
    ),
    Qari(
      id = 56, nameResource = R.string.qari_fares_abbad_gapless,
      url = "https://download.quranicaudio.com/quran/fares/reencode/",
      opusUrl = null, path = "fares_abbad",
      hasGaplessAlternative = false, db = "fares_abbad"
    ),
    Qari(
      id = 57, nameResource = R.string.qari_khalifa_taniji_gapless,
      url = "https://mirrors.quranicaudio.com/mp3quran/khalifah_taniji/",
      opusUrl = null, path = "khalifa_taniji",
      hasGaplessAlternative = false, db = "khalifa_taniji"
    ),
    Qari(
      id = 58, nameResource = R.string.qari_abdullah_matroud_gapless,
      url = "https://download.quranicaudio.com/quran/abdullah_matroud/reencode/",
      opusUrl = null, path = "abdullah_matroud",
      hasGaplessAlternative = false, db = "abdullah_matroud"
    ),
    Qari(
      id = 59, nameResource = R.string.qari_salah_bukhatir_gapless,
      url = "https://download.quranicaudio.com/quran/salaah_bukhaatir/",
      opusUrl = "https://download.quranicaudio.com/quran/salaah_bukhaatir/opus/", path = "salah_bukhatir",
      hasGaplessAlternative = false, db = "salah_bukhatir"
    ),
    Qari(
      id = 60, nameResource = R.string.qari_hudhayfi_gapless,
      url = "https://mirrors.quranicaudio.com/qurancomplex/ali_hudhayfi/",
      opusUrl = null, path = "ali_hudhayfi",
      hasGaplessAlternative = false, db = "ali_hudhayfi"
    ),
    Qari(
      id = 61, nameResource = R.string.qari_khaled_muhanna_gapless,
      url = "https://mirrors.quranicaudio.com/qurancomplex/khaled_almuhanna/",
      opusUrl = null, path = "khaled_almuhanna",
      hasGaplessAlternative = false, db = "khaled_almuhanna"
    ),
    Qari(
      id = 62, nameResource = R.string.qari_husary_mujawwad_gapless,
      url = "https://download.quranicaudio.com/quran/generated/husary_mujawwad/",
      opusUrl = "https://download.quranicaudio.com/quran/generated/husary_mujawwad/opus/", path = "husary_mujawwad",
      hasGaplessAlternative = false, db = "husary_mujawwad"
    ),
    Qari(
      id = 63, nameResource = R.string.qari_husary_muallim_gapless,
      url = "https://download.quranicaudio.com/quran/generated/husary_muallim/",
      opusUrl = null, path = "husary_muallim",
      hasGaplessAlternative = false, db = "husary_muallim"
    ),
    Qari(
      id = 64, nameResource = R.string.qari_ibrahim_alakhdar_gapless,
      url = "https://mirrors.quranicaudio.com/qurancomplex/ibrahim_alakhdar/",
      opusUrl = null, path = "ibrahim_alakhdar",
      hasGaplessAlternative = false, db = "ibrahim_alakhdar"
    ),
    Qari(
      id = 65, nameResource = R.string.qari_muaiqly_gapless,
      url = "https://mirrors.quranicaudio.com/qurancomplex/maher_muaiqly/",
      opusUrl = null, path = "muaiqly_kfgqpc",
      hasGaplessAlternative = false, db = "muaiqly_kfgqpc"
    ),
    Qari(
      id = 66, nameResource = R.string.qari_yasser_salama_hadr_gapless,
      url = "https://mirrors.quranicaudio.com/ayahapp/yasser_salama_hadr/",
      opusUrl = null, path = "yasser_salama_hadr",
      hasGaplessAlternative = false, db = "yasser_salama_hadr"
    ),
    Qari(
      id = 67, nameResource = R.string.qari_khalid_qahtani_gapless,
      url = "https://download.quranicaudio.com/quran/khaalid_al-qahtaanee/",
      opusUrl = "https://download.quranicaudio.com/quran/khaalid_al-qahtaanee/opus/", path = "khalid_alqahtani",
      hasGaplessAlternative = false, db = "khalid_alqahtani"
    ),
    Qari(
      id = 68, nameResource = R.string.qari_mokhtasar_asmari_gapless,
      url = "https://mirrors.quranicaudio.com/ayahapp/mokhtasar_asmari/",
      opusUrl = null, path = "mokhtasar_asmari",
      hasGaplessAlternative = false, db = "mokhtasar_asmari"
    ),
    Qari(
      id = 69, nameResource = R.string.qari_alzain_ahmad_gapless,
      url = "https://mirrors.quranicaudio.com/ourquraan/alzain_mohammad_ahmad/",
      opusUrl = null, path = "alzain_mohammad_ahmad",
      hasGaplessAlternative = false, db = "alzain_mohammad_ahmad"
    ),
    Qari(
      id = 70, nameResource = R.string.qari_aloosi_gapless,
      url = "https://mirrors.quranicaudio.com/mp3quran/abdulrahman_aloosi/",
      opusUrl = null, path = "abdulrahman_aloosi",
      hasGaplessAlternative = false, db = "abdulrahman_aloosi"
    ),
    Qari(
      id = 71, nameResource = R.string.qari_muhammad_rashad_shereef_gapless,
      url = "https://mirrors.quranicaudio.com/mp3quran/muhammad_rashad_alshereef/",
      opusUrl = null, path = "muhammad_rashad_shereef",
      hasGaplessAlternative = false, db = "muhammad_rashad_shereef"
    ),
    Qari(
      id = 72, nameResource = R.string.qari_wadee3_alyamani_gapless,
      url = "https://download.quranicaudio.com/quran/wadee_hammadi_al-yamani/",
      opusUrl = "https://download.quranicaudio.com/quran/wadee_hammadi_al-yamani/opus/", path = "wadee3_alyamani",
      hasGaplessAlternative = false, db = "wadee3_alyamani"
    ),
    Qari(
      id = 73, nameResource = R.string.qari_minshawi_mujawwad_gapless,
      url = "https://download.quranicaudio.com/quran/minshawi_mujawwad/",
      opusUrl = "https://download.quranicaudio.com/quran/minshawi_mujawwad/opus/", path = "minshawi_mujawwad",
      hasGaplessAlternative = false, db = "minshawi_mujawwad"
    ),
    Qari(
      id = 74, nameResource = R.string.qari_ayman_suwaid_gapless,
      url = "https://mirrors.quranicaudio.com/tvquran/ayman_suwaid/",
      opusUrl = null, path = "dr_ayman_suwaid",
      hasGaplessAlternative = false, db = "ayman_suwaid"
    ),
    Qari(
      id = 75, nameResource = R.string.qari_hady_toure_gapless,
      url = "https://mirrors.quranicaudio.com/tvquran/hady_toure/",
      opusUrl = null, path = "hady_toure",
      hasGaplessAlternative = false, db = "hady_toure"
    ),
    Qari(
      id = 76, nameResource = R.string.qari_nabil_rifa3i_gapless,
      url = "https://download.quranicaudio.com/quran/nabil_rifa3i/reencode/",
      opusUrl = null, path = "nabil_rifa3i",
      hasGaplessAlternative = false, db = "nabil_rifa3i"
    ),
    Qari(
      id = 77, nameResource = R.string.qari_noreen_siddiq_gapless,
      url = "https://download.quranicaudio.com/quran/noreen_siddiq/reencode/",
      opusUrl = null, path = "noreen_siddiq",
      hasGaplessAlternative = false, db = "noreen_siddiq"
    ),
    Qari(
      id = 78, nameResource = R.string.qari_tawfeeq_as_sawaigh_gapless,
      url = "https://download.quranicaudio.com/quran/tawfeeq_bin_saeed-as-sawaaigh/",
      opusUrl = "https://download.quranicaudio.com/quran/tawfeeq_bin_saeed-as-sawaaigh/opus/", path = "tawfeeq_as_sawaigh",
      hasGaplessAlternative = false, db = "tawfeeq_as_sawaigh"
    ),
    Qari(
      id = 79, nameResource = R.string.qari_khalid_jalil_gapless,
      url = "https://mirrors.quranicaudio.com/tvquran/khalid_jalil/",
      opusUrl = null, path = "khalid_jalil",
      hasGaplessAlternative = false, db = "khalid_jalil"
    ),
    Qari(
      id = 80, nameResource = R.string.qari_alijon_qari_gapless,
      url = "https://download.quranicaudio.com/quran/alijon_qari/mp3/",
      opusUrl = "https://download.quranicaudio.com/quran/alijon_qari/opus/", path = "alijon_qari",
      hasGaplessAlternative = false, db = "alijon_qari"
    ),
    Qari(
      id = 81, nameResource = R.string.qari_badr_al_turki_gapless,
      url = "https://download.quranicaudio.com/quran/badr_al_turki/mp3/",
      opusUrl = "https://download.quranicaudio.com/quran/badr_al_turki/opus/", path = "badr_al_turki",
      hasGaplessAlternative = false, db = "badr_al_turki"
    ),
    Qari(
      id = 83, nameResource = R.string.qari_idrees_abkar_gapless,
      url = "https://download.quranicaudio.com/quran/idrees_abkar/mp3/",
      opusUrl = "https://download.quranicaudio.com/quran/idrees_abkar/opus/", path = "idrees_abkar",
      hasGaplessAlternative = false, db = "idrees_abkar"
    ),
    Qari(
      id = 84, nameResource = R.string.qari_luhaidan_gapless,
      url = "https://mirrors.quranicaudio.com/qurancentral/muhammad_al_luhaidan/mp3/",
      opusUrl = "https://mirrors.quranicaudio.com/qurancentral/muhammad_al_luhaidan/opus/", path = "muhammad_al_luhaidan",
      hasGaplessAlternative = false, db = "muhammad_al_luhaidan"
    ),
    Qari(
      id = 85, nameResource = R.string.qari_raad_al_kurdi_gapless,
      url = "https://download.quranicaudio.com/quran/raad_mohammad_al_kurdi/mp3/",
      opusUrl = "https://download.quranicaudio.com/quran/raad_mohammad_al_kurdi/opus/", path = "raad_al_kurdi",
      hasGaplessAlternative = false, db = "raad_al_kurdi"
    ),
    Qari(
      id = 86, nameResource = R.string.qari_abdulhadi_kanakeri_gapless,
      url = "https://mirrors.quranicaudio.com/mp3quran/abdulhadi_kanakeri/mp3/",
      opusUrl = "https://mirrors.quranicaudio.com/mp3quran/abdulhadi_kanakeri/opus/", path = "abdulhadi_kanakeri",
      hasGaplessAlternative = false, db = "abdulhadi_kanakeri"
    ),
    Qari(
      id = 87, nameResource = R.string.qari_ahmed_al_nufais_gapless,
      url = "https://mirrors.quranicaudio.com/ayahapp/ahmed_nufais/mp3/",
      opusUrl = "https://mirrors.quranicaudio.com/ayahapp/ahmed_nufais/opus/", path = "ahmed_nufais",
      hasGaplessAlternative = false, db = "ahmed_nufais"
    ),
    Qari(
      id = 88, nameResource = R.string.qari_peshawa_qadir_al_qurdi_gapless,
      url = "https://download.quranicaudio.com/quran/peshawa_qadir_al-kurdi/mp3/",
      opusUrl = "https://download.quranicaudio.com/quran/peshawa_qadir_al-kurdi/opus/", path = "peshawa_qadir_al-kurdi",
      hasGaplessAlternative = false, db = "peshawa_qadir_al-kurdi"
    ),
    Qari(
      id = 89, nameResource = R.string.qari_farman_shawani_gapless,
      url = "https://download.quranicaudio.com/quran/farman_shawani/mp3/",
      opusUrl = "https://download.quranicaudio.com/quran/farman_shawani/opus/", path = "farman_shawani",
      hasGaplessAlternative = false, db = "farman_shawani"
    )
  )
}
