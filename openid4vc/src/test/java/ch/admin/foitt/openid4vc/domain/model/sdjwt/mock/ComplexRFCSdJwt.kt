package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

/**
 * SD-JWT example can be found here: https://www.rfc-editor.org/rfc/rfc9901.html#name-complex-structured-sd-jwt
 */
internal object ComplexRFCSdJwt {
    /*
{
  "_sd": [
    "-aSznId9mWM8ocuQolCllsxVggq1-vHW4OtnhUtVmWw",
    "IKbrYNn3vA7WEFrysvbdBJjDDU_EvQIr0W18vTRpUSg",
    "otkxuT14nBiwzNJ3MPaOitOl9pVnXOaEHal_xkyNfKI"
  ],
  "iss": "https://issuer.example.com",
  "iat": 1683000000,
  "exp": 1883000000,
  "verified_claims": {
    "verification": {
      "_sd": [
        "7h4UE9qScvDKodXVCuoKfKBJpVBfXMF_TmAGVaZe3Sc",
        "vTwe3raHIFYgFA3xaUD2aMxFz5oDo8iBu05qKlOg9Lw"
      ],
      "trust_framework": "de_aml",
      "evidence": [
        {
          "...": "tYJ0TDucyZZCRMbROG4qRO5vkPSFRxFhUELc18CSl3k"
        }
      ]
    },
    "claims": {
      "_sd": [
        "RiOiCn6_w5ZHaadkQMrcQJf0Jte5RwurRs54231DTlo",
        "S_498bbpKzB6Eanftss0xc7cOaoneRr3pKr7NdRmsMo",
        "WNA-UNK7F_zhsAb9syWO6IIQ1uHlTmOU8r8CvJ0cIMk",
        "Wxh_sV3iRH9bgrTBJi-aYHNCLt-vjhX1sd-igOf_9lk",
        "_O-wJiH3enSB4ROHntToQT8JmLtz-mhO2f1c89XoerQ",
        "hvDXhwmGcJQsBCA2OtjuLAcwAMpDsaU0nkovcKOqWNE"
      ]
    }
  },
  "_sd_alg": "sha-256"
}
     */

    const val JSON = """
{
  "iss": "https://issuer.example.com",
  "exp": 1883000000,
  "verified_claims": {
    "verification": {
      "trust_framework": "de_aml",
      "time": "2012-04-23T18:25Z",
      "verification_process": "f24c6f-6d3f-4ec5-973e-b0d8506f3bc7",
      "evidence": [
        {
          "type": "document",
          "method": "pipp",
          "time": "2012-04-22T11:30Z",
          "document": {
            "type": "idcard",
            "issuer": {
              "name": "Stadt Augsburg",
              "country": "DE"
            },
            "number": "53554554",
            "date_of_issuance": "2010-03-23",
            "date_of_expiry": "2020-03-22"
          }
        }
      ]
    },
    "claims": {
      "given_name": "Max",
      "family_name": "Müller",
      "nationalities": [
        "DE"
      ],
      "birthdate": "1956-01-28",
      "place_of_birth": {
        "country": "IS",
        "locality": "Þykkvabæjarklaustur"
      },
      "address": {
        "locality": "Maxstadt",
        "postal_code": "12344",
        "country": "DE",
        "street_address": "Weidenstraße 22"
      }
    }
  },
  "birth_middle_name": "Timotheus",
  "salutation": "Dr.",
  "msisdn": "49123456789"
}
    """

    const val JWT =
        "eyJhbGciOiAiRVMyNTYiLCAidHlwIjogImV4YW1wbGUrc2Qtand0In0.eyJfc2QiOiBbIi1hU3puSWQ5bVdNOG9jdVFvbENsbHN4VmdncTEtdkhXNE90bmhVdFZtV3ciLCAiSUticllObjN2QTdXRUZyeXN2YmRCSmpERFVfRXZRSXIwVzE4dlRScFVTZyIsICJvdGt4dVQxNG5CaXd6TkozTVBhT2l0T2w5cFZuWE9hRUhhbF94a3lOZktJIl0sICJpc3MiOiAiaHR0cHM6Ly9pc3N1ZXIuZXhhbXBsZS5jb20iLCAiaWF0IjogMTY4MzAwMDAwMCwgImV4cCI6IDE4ODMwMDAwMDAsICJ2ZXJpZmllZF9jbGFpbXMiOiB7InZlcmlmaWNhdGlvbiI6IHsiX3NkIjogWyI3aDRVRTlxU2N2REtvZFhWQ3VvS2ZLQkpwVkJmWE1GX1RtQUdWYVplM1NjIiwgInZUd2UzcmFISUZZZ0ZBM3hhVUQyYU14Rno1b0RvOGlCdTA1cUtsT2c5THciXSwgInRydXN0X2ZyYW1ld29yayI6ICJkZV9hbWwiLCAiZXZpZGVuY2UiOiBbeyIuLi4iOiAidFlKMFREdWN5WlpDUk1iUk9HNHFSTzV2a1BTRlJ4RmhVRUxjMThDU2wzayJ9XX0sICJjbGFpbXMiOiB7Il9zZCI6IFsiUmlPaUNuNl93NVpIYWFka1FNcmNRSmYwSnRlNVJ3dXJSczU0MjMxRFRsbyIsICJTXzQ5OGJicEt6QjZFYW5mdHNzMHhjN2NPYW9uZVJyM3BLcjdOZFJtc01vIiwgIldOQS1VTks3Rl96aHNBYjlzeVdPNklJUTF1SGxUbU9VOHI4Q3ZKMGNJTWsiLCAiV3hoX3NWM2lSSDliZ3JUQkppLWFZSE5DTHQtdmpoWDFzZC1pZ09mXzlsayIsICJfTy13SmlIM2VuU0I0Uk9IbnRUb1FUOEptTHR6LW1oTzJmMWM4OVhvZXJRIiwgImh2RFhod21HY0pRc0JDQTJPdGp1TEFjd0FNcERzYVUwbmtvdmNLT3FXTkUiXX19LCAiX3NkX2FsZyI6ICJzaGEtMjU2In0.QoWYWtikm-AtjmPnNVshbGXQl5raEz15PByTmZwfTQg9W2O3oR6j2tMmysTZZawdo6mNLR_PsZSI25qrUpiNTg"
    val SD_JWT = JWT + listOf(
        "WyIyR0xDNDJzS1F2ZUNmR2ZyeU5STjl3IiwgInRpbWUiLCAiMjAxMi0wNC0yM1QxODoyNVoiXQ",
        "WyJlbHVWNU9nM2dTTklJOEVZbnN4QV9BIiwgInZlcmlmaWNhdGlvbl9wcm9jZXNzIiwgImYyNGM2Zi02ZDNmLTRlYzUtOTczZS1iMGQ4NTA2ZjNiYzciXQ",
        "WyI2SWo3dE0tYTVpVlBHYm9TNXRtdlZBIiwgInR5cGUiLCAiZG9jdW1lbnQiXQ",
        "WyJlSThaV205UW5LUHBOUGVOZW5IZGhRIiwgIm1ldGhvZCIsICJwaXBwIl0",
        "WyJRZ19PNjR6cUF4ZTQxMmExMDhpcm9BIiwgInRpbWUiLCAiMjAxMi0wNC0yMlQxMTozMFoiXQ",
        "WyJBSngtMDk1VlBycFR0TjRRTU9xUk9BIiwgImRvY3VtZW50IiwgeyJ0eXBlIjogImlkY2FyZCIsICJpc3N1ZXIiOiB7Im5hbWUiOiAiU3RhZHQgQXVnc2J1cmciLCAiY291bnRyeSI6ICJERSJ9LCAibnVtYmVyIjogIjUzNTU0NTU0IiwgImRhdGVfb2ZfaXNzdWFuY2UiOiAiMjAxMC0wMy0yMyIsICJkYXRlX29mX2V4cGlyeSI6ICIyMDIwLTAzLTIyIn1d",
        "WyJQYzMzSk0yTGNoY1VfbEhnZ3ZfdWZRIiwgeyJfc2QiOiBbIjl3cGpWUFd1RDdQSzBuc1FETDhCMDZsbWRnVjNMVnliaEh5ZFFwVE55TEkiLCAiRzVFbmhPQU9vVTlYXzZRTU52ekZYanBFQV9SYy1BRXRtMWJHX3djYUtJayIsICJJaHdGcldVQjYzUmNacTl5dmdaMFhQYzdHb3doM08ya3FYZUJJc3dnMUI0IiwgIldweFE0SFNvRXRjVG1DQ0tPZURzbEJfZW11Y1lMejJvTzhvSE5yMWJFVlEiXX1d",
        "WyJHMDJOU3JRZmpGWFE3SW8wOXN5YWpBIiwgImdpdmVuX25hbWUiLCAiTWF4Il0",
        "WyJsa2x4RjVqTVlsR1RQVW92TU5JdkNBIiwgImZhbWlseV9uYW1lIiwgIk1cdTAwZmNsbGVyIl0",
        "WyJuUHVvUW5rUkZxM0JJZUFtN0FuWEZBIiwgIm5hdGlvbmFsaXRpZXMiLCBbIkRFIl1d",
        "WyI1YlBzMUlxdVpOYTBoa2FGenp6Wk53IiwgImJpcnRoZGF0ZSIsICIxOTU2LTAxLTI4Il0",
        "WyI1YTJXMF9OcmxFWnpmcW1rXzdQcS13IiwgInBsYWNlX29mX2JpcnRoIiwgeyJjb3VudHJ5IjogIklTIiwgImxvY2FsaXR5IjogIlx1MDBkZXlra3ZhYlx1MDBlNmphcmtsYXVzdHVyIn1d",
        "WyJ5MXNWVTV3ZGZKYWhWZGd3UGdTN1JRIiwgImFkZHJlc3MiLCB7ImxvY2FsaXR5IjogIk1heHN0YWR0IiwgInBvc3RhbF9jb2RlIjogIjEyMzQ0IiwgImNvdW50cnkiOiAiREUiLCAic3RyZWV0X2FkZHJlc3MiOiAiV2VpZGVuc3RyYVx1MDBkZmUgMjIifV0",
        "WyJIYlE0WDhzclZXM1FEeG5JSmRxeU9BIiwgImJpcnRoX21pZGRsZV9uYW1lIiwgIlRpbW90aGV1cyJd",
        "WyJDOUdTb3VqdmlKcXVFZ1lmb2pDYjFBIiwgInNhbHV0YXRpb24iLCAiRHIuIl0",
        "WyJreDVrRjE3Vi14MEptd1V4OXZndnR3IiwgIm1zaXNkbiIsICI0OTEyMzQ1Njc4OSJd",
    ).shuffled().toDisclosures()
}
