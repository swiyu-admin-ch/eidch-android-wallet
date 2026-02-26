package ch.admin.foitt.openid4vc.domain.model.issuerMetadata

import android.webkit.URLUtil
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofType
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.utils.JsonError
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MetadataTest {

    private val json = SafeJsonTestInstance.safeJson

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `issuer metadata test`() = runTest {
        mockkStatic(URLUtil::class)
        every { URLUtil.isHttpsUrl(any()) } returns true
        json.safeDecodeStringTo<IssuerCredentialInfo>(createMetadata()).assertOk()
    }

    @Test
    fun `unsupported signing algorithm is ignored`() = runTest {
        mockkStatic(URLUtil::class)
        every { URLUtil.isHttpsUrl(any()) } returns true
        val result = json.safeDecodeStringTo<IssuerCredentialInfo>(
            createMetadata(listOf(SigningAlgorithm.ES256.stdName, "unsupportedAlgo"))
        ).assertOk()

        val parsedSigningAlgorithms =
            result.credentialConfigurations.first().proofTypesSupported[ProofType.JWT]?.proofSigningAlgValuesSupported

        assertEquals(listOf(SigningAlgorithm.ES256), parsedSigningAlgorithms)
    }

    @Test
    fun `issuer metadata test with not valid https url returns error`() = runTest {
        mockkStatic(URLUtil::class)
        every { URLUtil.isHttpsUrl(any()) } returns false

        val result = json.safeDecodeStringTo<IssuerCredentialInfo>(createMetadata())

        result.assertErrorType(JsonError.Unexpected::class)
    }

    fun createMetadata(
        signingAlgorithms: List<String> = listOf(SigningAlgorithm.ES256.stdName, SigningAlgorithm.ES512.stdName),
    ): String {
        val signingAlgorithmsString = signingAlgorithms.joinToString(separator = ",") { it }

        return """
            {
               "credential_issuer":"https://issuer-agent.domain.ch",
               "credential_response_encryption":{
                  "encryption_required":false,
                  "alg_values_supported":[
                     "RSA-OAEP-256",
                     "ECDH-ES+A128KW"
                  ],
                  "enc_values_supported":[
                     "A128CBC-HS256"
                  ]
               },
               "display":[
                  {
                     "name":"Strassenverkehrsamt Appenzell Ausserrhoden",
                     "locale":"de-CH",
                     "logo":{
                        "uri":"data:image/png;base64,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"
                     }
                  },
                  {
                     "name":"Road traffic office Appenzell Ausserrhoden",
                     "locale":"en-US",
                     "logo":{
                        "uri":"data:image/png;base64,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"
                     }
                  }
               ],
               "credential_configurations_supported":{
                  "elfa-sdjwt":{
                     "format":"vc+sd-jwt",
                     "cryptographic_binding_methods_supported":[
                        "jwk",
                        "did:jwk"
                     ],
                     "credential_signing_alg_values_supported":[
                        "Ed25519VerificationKey2020"
                     ],
                     "proof_types_supported":{
                        "jwt":{
                           "proof_signing_alg_values_supported":[
                              $signingAlgorithmsString
                           ],
                           "key_attestations_required": {
                             "key_storage": [
                                "iso_18045_high",
                                "iso_18045_moderate"
                             ],
                             "user_authentication": [
                                "iso_18045_high"
                             ]
                           }
                        }
                     },
                     "display":[
                        {
                           "name":"REF: Demo-Lernfahrausweis",
                           "locale":"de-CH",
                           "logo":{
                              "uri":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg=="
                           },
                           "description":"Kategorie B",
                           "background_color":"#ff69b4"
                        },
                        {
                           "name":"REF: Demo digital learner's permit",
                           "locale":"en-US",
                           "logo":{
                              "uri":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAACxIAAAsSAdLdfvwAAACaSURBVEhLYxgFo2DQA0YoTRB89v0sfp/7PsMr7lcMLnNdXgGF/kNkSANMUBov+M/wn/Hfv38vmBmZX/xl/vtiVf0qVqgUyYAoC6kJRi2kOhi1kOpg1EKqA7pbSFThDSpLP3l/+veE7wnDE14gxkG/534vxmDH8BqqDSsYxEH6n+EpkARjoI9R8X8IzfCT4R9Y7SgYBUMYMDAAAMQ3Q6IEloWyAAAAAElFTkSuQmCC"
                           },
                           "description":"Category B",
                           "background_color":"#4A412A"
                        }
                     ],
                     "vct":"elfa-sdjwt",
                     "claims":{
                        "id":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Holder binding key"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Holder binding key"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Holder binding key"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Holder binding key"
                              }
                           ]
                        },
                        "lastName":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Name"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Nom de famille"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Cognome"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Last name"
                              }
                           ]
                        },
                        "firstName":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Vorname"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Prénom"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Nome"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"First name"
                              }
                           ]
                        },
                        "dateOfBirth":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Geburtsdatum"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Date de naissance"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Data di nascita"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Date of birth"
                              }
                           ]
                        },
                        "hometown":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Heimatort"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Ville natale"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Città natale"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Home town"
                              }
                           ]
                        },
                        "registrationNumber":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"RegNr"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Numéro d'enregistrement"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Numero di registrazione"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"RegNb"
                              }
                           ]
                        },
                        "dateOfExpiration":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Gültigkeit"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Date d'expiration"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Data di scadenza"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Validity"
                              }
                           ]
                        },
                        "issuerEntity":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Ausstellbehörde"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Entité émettrice"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Ente emittente"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Issuing authority"
                              }
                           ]
                        },
                        "issuerEntityDate":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Ausstellbehörde (Datum)"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Date de l'entité émettrice"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Data ente emittente"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Issuing authority (date)"
                              }
                           ]
                        },
                        "signatureImage":{
                           "mandatory":false,
                           "value_type":"image/png",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Unterschrift"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Image de la signature"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Immagine della firma"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Signature"
                              }
                           ]
                        },
                        "photoImage":{
                           "mandatory":true,
                           "value_type":"image/png",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Foto"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Photo"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Foto"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Photo"
                              }
                           ]
                        },
                        "categoryCode":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Kategorie"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Code de catégorie"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Codice categoria"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Category"
                              }
                           ]
                        },
                        "categoryRestrictions":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Auflagen/Verfügungen/Beschränkung auf der Kat"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Restrictions de catégorie"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Restrizioni di categoria"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Category Restrictions"
                              }
                           ]
                        },
                        "restrictionsA":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Auflagen/Verfügungen/Beschränkung A"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Restrictions A"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Restrizioni A"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Restrictions A"
                              }
                           ]
                        },
                        "restrictionsB":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Auflagen/Verfügungen/Beschränkung B"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Restrictions B"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Restrizioni B"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Restrictions B"
                              }
                           ]
                        },
                        "faberPin":{
                           "mandatory":false,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Faber Pin"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Pin Faber"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Pin Faber"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Faber Pin"
                              }
                           ]
                        },
                        "policeQRImage":{
                           "mandatory":false,
                           "value_type":"image/png",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Police QR Code"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Code QR de la police"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Codice QR della polizia"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Police QR Code"
                              }
                           ]
                        },
                        "credentialNumber":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Credentialnummer"
                              },
                              {
                                 "locale":"fr-CH",
                                 "name":"Numéro de justificatif"
                              },
                              {
                                 "locale":"it-CH",
                                 "name":"Numero del credenziale"
                              },
                              {
                                 "locale":"en-US",
                                 "name":"Credential Number"
                              }
                           ]
                        }
                     },
                     "order":[
                        "photoImage",
                        "id",
                        "lastName",
                        "firstName",
                        "dateOfBirth",
                        "hometown",
                        "issuerEntity",
                        "issuerEntityDate",
                        "dateOfExpiration",
                        "categoryCode",
                        "categoryRestrictions",
                        "restrictionsA",
                        "restrictionsB",
                        "registrationNumber",
                        "faberPin",
                        "credentialNumber",
                        "policeQRImage",
                        "signatureImage"
                     ]
                  },
                  "university_example_sd_jwt":{
                     "format":"vc+sd-jwt",
                     "credential_signing_alg_values_supported":[
                        "ES256"
                     ],
                     "proof_types_supported":{
                        "jwt":{
                           "proof_signing_alg_values_supported":[
                              "ES256",
                              "ES512"
                           ]
                        }
                     },
                     "vct":"university_example_sd_jwt",
                     "claims":{
                        "degree":{
                           "type":{
                              "mandatory":true,
                              "value_type":"string",
                              "display":[
                                 {
                                    "locale":"de-CH",
                                    "name":"Abschluss Typ"
                                 }
                              ]
                           },
                           "name":{
                              "mandatory":true,
                              "value_type":"string",
                              "display":[
                                 {
                                    "locale":"de-CH",
                                    "name":"Diplomtitle"
                                 }
                              ]
                           },
                           "average_grade":{
                              "mandatory":false,
                              "value_type":"number",
                              "display":[
                                 {
                                    "locale":"de-CH",
                                    "name":"Notendurchschnitt"
                                 }
                              ]
                           }
                        }
                     }
                  },
                  "unbound_example_sd_jwt":{
                     "format":"vc+sd-jwt",
                     "credential_signing_alg_values_supported":[
                        "BBS2023"
                     ],
                     "vct":"unbound_example_sd_jwt",
                     "claims":{
                        "animal":{
                           "mandatory":true,
                           "value_type":"string",
                           "display":[
                              {
                                 "locale":"de-CH",
                                 "name":"Tier"
                              }
                           ]
                        }
                     }
                  }
               },
               "credential_endpoint":"https://issuer-agent.domain.ch/credential"
            }
        """.trimIndent()
    }
}
