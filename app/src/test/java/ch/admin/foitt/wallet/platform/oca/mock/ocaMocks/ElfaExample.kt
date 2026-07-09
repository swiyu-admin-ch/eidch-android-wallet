package ch.admin.foitt.wallet.platform.oca.mock.ocaMocks

val elfaCaptureBase = """
    {
        "type": "spec/capture_base/1.0",
        "digest": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
        "attributes": {
            "main": "refs:ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz"
        }
    }
""".trimIndent()

val elfaExample = """
    {
        "profile_version": "swiss-profile-vc:1.0.0",
        "capture_bases": [
            $elfaCaptureBase,
            {
                "type": "spec/capture_base/1.0",
                "digest": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "attributes": {
                    "lastName": "Text",
                    "firstName": "Text",
                    "dateOfBirth": "DateTime",
                    "hometown": "Text",
                    "dateOfExpiration": "DateTime",
                    "issuerEntity": "Text",
                    "issuerEntityDate": "DateTime",
                    "signatureImage": "Binary",
                    "photoImage": "Binary",
                    "policeQRImage": "Binary",
                    "categoryCode": "Text",
                    "categoryIcon": "Binary",
                    "categoryRestrictions": "Text",
                    "restrictionsA": "Text",
                    "restrictionsB": "Text",
                    "faberPin": "Numeric",
                    "licenceNumber": "Numeric"
                }
            }
        ],
        "overlays": [
            {
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "type": "spec/overlays/character_encoding/1.0",
                "default_character_encoding": "utf-8",
                "attribute_character_encoding": {
                    "signatureImage": "base64",
                    "photoImage": "base64",
                    "policeQRImage": "base64",
                    "categoryIcon": "base64"
                }
            },
            {
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "type": "spec/overlays/format/1.0",
                "attribute_formats": {
                    "dateOfBirth": "YYYY-MM-DD",
                    "dateOfExpiration": "YYYY-MM-DD",
                    "issuerEntityDate": "YYYY-MM-DD",
                    "signatureImage": "image/png",
                    "photoImage": "image/png",
                    "policeQRImage": "image/png",
                    "categoryIcon": "image/png"
                }
            },
            {
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "type": "spec/overlays/standard/1.0",
                "attr_standards": {
                    "dateOfBirth": "urn:iso:std:iso:8601",
                    "dateOfExpiration": "urn:iso:std:iso:8601",
                    "issuerEntityDate": "urn:iso:std:iso:8601",
                    "signatureImage": "urn:ietf:rfc:2083",
                    "photoImage": "urn:ietf:rfc:2083",
                    "policeQRImage": "urn:ietf:rfc:2083",
                    "categoryIcon": "urn:ietf:rfc:2083"
                }
            },
            {
                "type": "extend/overlays/data_source/2.0",
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "format": "vc+sd-jwt",
                "attribute_sources": {
                    "lastName": [
                        "lastName"
                    ],
                    "firstName": [
                        "firstName"
                    ],
                    "dateOfBirth": [
                        "dateOfBirth"
                    ],
                    "hometown": [
                        "hometown"
                    ],
                    "dateOfExpiration": [
                        "dateOfExpiration"
                    ],
                    "issuerEntity": [
                        "issuerEntity"
                    ],
                    "issuerEntityDate": [
                        "issuerEntityDate"
                    ],
                    "signatureImage": [
                        "signatureImage"
                    ],
                    "photoImage": [
                        "photoImage"
                    ],
                    "policeQRImage": [
                        "policeQRImage"
                    ],
                    "categoryCode": [
                        "categoryCode"
                    ],
                    "categoryIcon": [
                        "categoryIcon"
                    ],
                    "categoryRestrictions": [
                        "categoryRestrictions"
                    ],
                    "restrictionsA": [
                        "restrictionsA"
                    ],
                    "restrictionsB": [
                        "restrictionsB"
                    ],
                    "faberPin": [
                        "faberPin"
                    ],
                    "licenceNumber": [
                        "licenceNumber"
                    ]
                }
            },
            {
                "type": "spec/overlays/meta/1.0",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "de",
                "name": "REF: Lernfahrausweis",
                "description": "Elektronischer Lernfahrausweis"
            },
            {
                "type": "spec/overlays/meta/1.0",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "en",
                "name": "REF: Learner-driver permit",
                "description": "Electronic learner-driver permit"
            },
            {
                "type": "spec/overlays/meta/1.0",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "fr",
                "name": "REF: Permis d'élève conducteur",
                "description": "Permis d'élève conducteur électronique"
            },
            {
                "type": "spec/overlays/meta/1.0",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "it",
                "name": "REF: Licenza per allievo conducente",
                "description": "Licenza per allievo conducente elettronica"
            },
            {
                "type": "spec/overlays/meta/1.0",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "rm",
                "name": "REF: Permiss per emprender a manischar",
                "description": "Permiss per emprender a manischar electronic"
            },
            {
                "type": "aries/overlays/branding/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "de",
                "logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg==",
                "primary_background_color": "#007AFF",
                "primary_field": "Kategorie {{refs:ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz:categoryCode}}"
            },
            {
                "type": "aries/overlays/branding/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "en",
                "logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg==",
                "primary_background_color": "#007AEF",
                "primary_field": "Category {{refs:ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz:categoryCode}}"
            },
            {
                "type": "aries/overlays/branding/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "fr",
                "logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg==",
                "primary_background_color": "#007AEF",
                "primary_field": "Catégorie {{refs:ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz:categoryCode}}"
            },
            {
                "type": "aries/overlays/branding/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "it",
                "logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg==",
                "primary_background_color": "#007AEF",
                "primary_field": "Categoria {{refs:ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz:categoryCode}}"
            },
            {
                "type": "aries/overlays/branding/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "rm",
                "logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg==",
                "primary_background_color": "#007AEF",
                "primary_field": "Categoria {{refs:ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz:categoryCode}}"
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "de",
                "attribute_labels": {
                    "main": "Details"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "en",
                "attribute_labels": {
                    "main": "Details"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "fr",
                "attribute_labels": {
                    "main": "Détails"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "it",
                "attribute_labels": {
                    "main": "Dettagli"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "IBiT1Hjy50PCdVDp-EQLDePluS93MmJrWg5hKePPVzdq",
                "language": "rm",
                "attribute_labels": {
                    "main": "Detagls"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "language": "de",
                "attribute_labels": {
                    "lastName": "Name",
                    "firstName": "Vorname",
                    "dateOfBirth": "Geburtsdatum",
                    "hometown": "Heimatort",
                    "dateOfExpiration": "Ablaufdatum",
                    "issuerEntity": "Ausstellende Behörde",
                    "issuerEntityDate": "Ausstelldatum",
                    "signatureImage": "Unterschrift",
                    "photoImage": "Foto",
                    "policeQRImage": "Polizeikontrolle, QR-Code",
                    "categoryCode": "Kategorie",
                    "categoryIcon": "Kategorie Piktogramm",
                    "categoryRestrictions": "Zusatzangaben auf der Kategorie",
                    "restrictionsA": "Zusatzangaben (A)",
                    "restrictionsB": "Zusatzangaben (B)",
                    "faberPin": "FABER-PIN",
                    "licenceNumber": "Nummer des Lernfahrausweises"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "language": "en",
                "attribute_labels": {
                    "lastName": "Family name",
                    "firstName": "First name",
                    "dateOfBirth": "Date of birth",
                    "hometown": "Place of origin",
                    "dateOfExpiration": "Expiry date",
                    "issuerEntity": "Issuing authority",
                    "issuerEntityDate": "Date of issue",
                    "signatureImage": "Signature",
                    "photoImage": "Photo",
                    "policeQRImage": "Police control, QR code",
                    "categoryCode": "Category",
                    "categoryIcon": "Category pictogram",
                    "categoryRestrictions": "Additional information for the category",
                    "restrictionsA": "Additional information (A)",
                    "restrictionsB": "Additional information (B)",
                    "faberPin": "FABER PIN",
                    "licenceNumber": "Learner-driver permit number"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "language": "fr",
                "attribute_labels": {
                    "lastName": "Nom",
                    "firstName": "Prénom",
                    "dateOfBirth": "Date de naissance",
                    "hometown": "Lieu d'origine",
                    "dateOfExpiration": "Date d'échéance",
                    "issuerEntity": "Autorité d'émission",
                    "issuerEntityDate": "Date de délivrance",
                    "signatureImage": "Signature",
                    "photoImage": "Photo",
                    "policeQRImage": "Contrôle de police, QR-code",
                    "categoryCode": "Catégorie",
                    "categoryIcon": "Catégorie pictogramme",
                    "categoryRestrictions": "Indications complémentaires relatives à la catégorie",
                    "restrictionsA": "Indications complémentaires (A)",
                    "restrictionsB": "Indications complémentaires (B)",
                    "faberPin": "NIP FABER",
                    "licenceNumber": "Numéro du permis d'élève conducteur"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "language": "it",
                "attribute_labels": {
                    "lastName": "Cognome",
                    "firstName": "Nome",
                    "dateOfBirth": "Data di nascita",
                    "hometown": "Luogo di origine",
                    "dateOfExpiration": "Data di scadenza",
                    "issuerEntity": "Autorità di rilascio",
                    "issuerEntityDate": "Data di rilascio",
                    "signatureImage": "Firma",
                    "photoImage": "Foto",
                    "policeQRImage": "Controllo della polizia, codice QR",
                    "categoryCode": "Categoria",
                    "categoryIcon": "Categoria pittogramma",
                    "categoryRestrictions": "Dati supplementari relativi alla categoria",
                    "restrictionsA": "Dati supplementari (A)",
                    "restrictionsB": "Dati supplementari (B)",
                    "faberPin": "PIN FABER",
                    "licenceNumber": "Numero della licenza per allievo conducente"
                }
            },
            {
                "type": "spec/overlays/label/1.1",
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "language": "rm",
                "attribute_labels": {
                    "lastName": "Num",
                    "firstName": "Prenum",
                    "dateOfBirth": "Data da naschientscha",
                    "hometown": "Lieu d'origin",
                    "dateOfExpiration": "Data da scadenza",
                    "issuerEntity": "Autoridad d'emissiun",
                    "issuerEntityDate": "Data d'emissiun",
                    "signatureImage": "Suttascripziun",
                    "photoImage": "Fotografia",
                    "policeQRImage": "Controlla da la polizia, code QR",
                    "categoryCode": "Categoria",
                    "categoryIcon": "Categoria pictogram",
                    "categoryRestrictions": "Indicaziuns supplementaras davart la categoria",
                    "restrictionsA": "Indicaziuns supplementaras (A)",
                    "restrictionsB": "Indicaziuns supplementaras (B)",
                    "faberPin": "PIN FABER",
                    "licenceNumber": "Numer dal permiss per emprender a manischar"
                }
            },
            {
                "type": "extend/overlays/order/1.0",
                "capture_base": "ILFKGCCSyscqnYnMYl6QR-zD0UoNHuNqPpm9-5yiGMLz",
                "attribute_orders": {
                    "policeQRImage": 1,
                    "photoImage": 2,
                    "lastName": 3,
                    "firstName": 4,
                    "dateOfBirth": 5,
                    "hometown": 6,
                    "issuerEntityDate": 7,
                    "dateOfExpiration": 8,
                    "issuerEntity": 9,
                    "categoryCode": 10,
                    "categoryIcon": 11,
                    "categoryRestrictions": 12,
                    "restrictionsA": 13,
                    "restrictionsB": 14,
                    "licenceNumber": 15,
                    "faberPin": 16,
                    "signatureImage": 17
                }
            }
        ]
    }
""".trimIndent()
