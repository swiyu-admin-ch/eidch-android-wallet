package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object ComplexSdJwt {
    /*
{
   "key_undisclosed":"value_undisclosed",
   "_sd":[
      "psfvd7B2jbfN8_tfkRmen19XTEKyoP1jO9DHevIG0Iw",
      "decoym52jwU68uFqCEA_ymFAn3gPend70mEZnLIhR_s",
      "8Xtovm52jwU68uFqCEA_ymFAn3gPend70mEZnLIhR_s",
      "mCj_kUcQxNFyhT4BFePPaQZ3X141PMRwHLB5brWok_c",
      "decoyk02tRcB8f4wu7BGZ2Qq7MnPUP9n1-yLDqvKY0g"
   ],
   "key_array_undisclosed":[
      "value_array_undisclosed_1",
      "value_array_undisclosed_2"
   ],
   "key_array_partly_disclosed":[
      {
         "...":"kJK4cVfgVT_yItNsNe39lX7TwaIHaHC9BatszCXt2ww"
      },
      "value_array_partly_disclosed_2",
      {
         "...":"decoyVfgVT_yItNsNe39lX7TwaIHaHC9BatszCXt2ww"
      },
      {
         "...":"0N6j5F4x_RE9ymmtXkPqMERbsOSVABJ_NTvuOKUgxnc"
      }
   ],
   "key_object_undisclosed":{
      "key_nested_object_1":"value_object_undisclosed_1",
      "key_nested_object_2":"value_object_undisclosed_2"
   },
   "key_object_partly_disclosed":{
      "key_object_partly_disclosed_1_1":"value_object_partly_disclosed_1_1",
      "_sd":[
         "decoyLDpaCc_1y4C5zJ58MUD6J4opcgOU7PeFJiiV28",
         "SEWSLxmfTpFVGrm9DbxpNk3epLdivSv2TFSWgoBnG_k",
         "raXs5cEG0_2uWLK5PIW0N5I8rrzXk7H3tgKBBOMoXD8",
         "decoyKqp7zAwU0LacYMtCJICDWvU8hB_mpmEpR4a3Eg"
      ]
   },
   "_sd_alg":"sha-256"
}
     */

    // ["salt_flat", "key_flat", "value_flat"]
    // psfvd7B2jbfN8_tfkRmen19XTEKyoP1jO9DHevIG0Iw
    private const val FLAT_DISCLOSURE = "WyJzYWx0X2ZsYXQiLCAia2V5X2ZsYXQiLCAidmFsdWVfZmxhdCJd"

    // ["salt_flat_array", "key_flat_array", ["value_flat_array_1", "value_flat_array_2"]]
    // 8Xtovm52jwU68uFqCEA_ymFAn3gPend70mEZnLIhR_s
    private const val FLAT_ARRAY_DISCLOSURE =
        "WyJzYWx0X2ZsYXRfYXJyYXkiLCAia2V5X2ZsYXRfYXJyYXkiLCBbInZhbHVlX2ZsYXRfYXJyYXlfMSIsICJ2YWx1ZV9mbGF0X2FycmF5XzIiXV0"

    // ["salt_flat_object", "key_flat_object", {"key_nested_object_1":"value_flat_object_1","key_nested_object_2":"value_flat_object_2"}]
    // mCj_kUcQxNFyhT4BFePPaQZ3X141PMRwHLB5brWok_c
    private const val FLAT_OBJECT_DISCLOSURE =
        "WyJzYWx0X2ZsYXRfb2JqZWN0IiwgImtleV9mbGF0X29iamVjdCIsIHsia2V5X25lc3RlZF9vYmplY3RfMSI6InZhbHVlX2ZsYXRfb2JqZWN0XzEiLCJrZXlfbmVzdGVkX29iamVjdF8yIjoidmFsdWVfZmxhdF9vYmplY3RfMiJ9XQ"

    // ["salt_array_partly_disclosed_1", "value_array_partly_disclosed_1"]
    // kJK4cVfgVT_yItNsNe39lX7TwaIHaHC9BatszCXt2ww
    private const val ARRAY_PARTLY_DISCLOSED_DISCLOSURE_1 =
        "WyJzYWx0X2FycmF5X3BhcnRseV9kaXNjbG9zZWRfMSIsICJ2YWx1ZV9hcnJheV9wYXJ0bHlfZGlzY2xvc2VkXzEiXQ"

    // ["salt_array_partly_disclosed_3", "value_array_partly_disclosed_3"]
    // 0N6j5F4x_RE9ymmtXkPqMERbsOSVABJ_NTvuOKUgxnc
    const val ARRAY_PARTLY_DISCLOSED_DISCLOSURE_3 =
        "WyJzYWx0X2FycmF5X3BhcnRseV9kaXNjbG9zZWRfMyIsICJ2YWx1ZV9hcnJheV9wYXJ0bHlfZGlzY2xvc2VkXzMiXQ"

    // ["salt_object_partly_disclosed_1_2", "key_object_partly_disclosed_1_2", {"key_object_partly_disclosed_1_2_2":"value_object_partly_disclosed_1_2_2", "_sd":["E5KxSZoO8jVOckUoW7zldMM7TFWDs1obK_ES1XuWJ8E", "VIas7gCeBScyUWeeHXYvrvyq47aHsBR6I-MohC5umSw", "decoyfukdVfCExiTsM7dsoUMh_-FrLn-aBgPidHbAVU"]}]
    // SEWSLxmfTpFVGrm9DbxpNk3epLdivSv2TFSWgoBnG_k
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMiIsICJrZXlfb2JqZWN0X3BhcnRseV9kaXNjbG9zZWRfMV8yIiwgeyJrZXlfb2JqZWN0X3BhcnRseV9kaXNjbG9zZWRfMV8yXzIiOiJ2YWx1ZV9vYmplY3RfcGFydGx5X2Rpc2Nsb3NlZF8xXzJfMiIsICJfc2QiOlsiRTVLeFNab084alZPY2tVb1c3emxkTU03VEZXRHMxb2JLX0VTMVh1V0o4RSIsICJWSWFzN2dDZUJTY3lVV2VlSFhZdnJ2eXE0N2FIc0JSNkktTW9oQzV1bVN3IiwgImRlY295ZnVrZFZmQ0V4aVRzTTdkc29VTWhfLUZyTG4tYUJnUGlkSGJBVlUiXX1d"

    // ["salt_object_partly_disclosed_1_2_1", "key_object_partly_disclosed_1_2_1", "value_object_partly_disclosed_1_2_1"]
    // E5KxSZoO8jVOckUoW7zldMM7TFWDs1obK_ES1XuWJ8E
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_1 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8xIiwgImtleV9vYmplY3RfcGFydGx5X2Rpc2Nsb3NlZF8xXzJfMSIsICJ2YWx1ZV9vYmplY3RfcGFydGx5X2Rpc2Nsb3NlZF8xXzJfMSJd"

    // ["salt_object_partly_disclosed_1_2_3", "key_object_partly_disclosed_1_2_3", [{"...":"decoygsqyTzjei_kdhpICR3bmao-dRKKdLSFOUkQZNk"}, {"...":"NLQ9qIhd-n3O6bOEjwwq10I19orfVs2qLFu7XcrHLHM"}, {"...":"MRxfTo0MGKrJSJvkPX-i-bm4-tyNP2WbAdCA5P885nc"}]]
    // VIas7gCeBScyUWeeHXYvrvyq47aHsBR6I-MohC5umSw
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8zIiwgImtleV9vYmplY3RfcGFydGx5X2Rpc2Nsb3NlZF8xXzJfMyIsIFt7Ii4uLiI6ImRlY295Z3NxeVR6amVpX2tkaHBJQ1IzYm1hby1kUktLZExTRk9Va1FaTmsifSwgeyIuLi4iOiJOTFE5cUloZC1uM082Yk9Fand3cTEwSTE5b3JmVnMycUxGdTdYY3JITEhNIn0sIHsiLi4uIjoiTVJ4ZlRvME1HS3JKU0p2a1BYLWktYm00LXR5TlAyV2JBZENBNVA4ODVuYyJ9XV0"

    // ["salt_object_partly_disclosed_1_2_3_1_1", {"key_object_partly_disclosed_1_2_3_1":["value_object_partly_disclosed_1_2_3_1_1_1"]}]
    // NLQ9qIhd-n3O6bOEjwwq10I19orfVs2qLFu7XcrHLHM
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_1 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8zXzFfMSIsIHsia2V5X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8zXzEiOlsidmFsdWVfb2JqZWN0X3BhcnRseV9kaXNjbG9zZWRfMV8yXzNfMV8xXzEiXX1d"

    // ["salt_object_partly_disclosed_1_2_3_2", {"_sd":["decoyOtvbtDg7myA5ip43aLkcNAnETfr44TrisiJ-d0", "WKDl2n6sdUM9MBhP6BGwOfAc_1kWnrujY7FbpgPOFSE"]}]
    // MRxfTo0MGKrJSJvkPX-i-bm4-tyNP2WbAdCA5P885nc
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_2 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8zXzIiLCB7Il9zZCI6WyJkZWNveU90dmJ0RGc3bXlBNWlwNDNhTGtjTkFuRVRmcjQ0VHJpc2lKLWQwIiwgIldLRGwybjZzZFVNOU1CaFA2Qkd3T2ZBY18xa1ducnVqWTdGYnBnUE9GU0UiXX1d"

    // ["salt_object_partly_disclosed_1_2_3_2_1", "key_object_partly_disclosed_1_2_3_1", [{"...":"H8Vy9zYKUnru74Z5RNzFtrgHWqg-7t5v5zBVAXeQhYM"}, {"...":"decoyzYKUnru74Z5RNzFtrgHWqg-7t5v5zBVAXeQhYM"}]]
    // WKDl2n6sdUM9MBhP6BGwOfAc_1kWnrujY7FbpgPOFSE
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_2_1 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8zXzJfMSIsICJrZXlfb2JqZWN0X3BhcnRseV9kaXNjbG9zZWRfMV8yXzNfMSIsIFt7Ii4uLiI6Ikg4Vnk5ellLVW5ydTc0WjVSTnpGdHJnSFdxZy03dDV2NXpCVkFYZVFoWU0ifSwgeyIuLi4iOiJkZWNveXpZS1VucnU3NFo1Uk56RnRyZ0hXcWctN3Q1djV6QlZBWGVRaFlNIn1dXQ"

    // ["salt_object_partly_disclosed_1_2_3_2_1_1", "value_object_partly_disclosed_1_2_3_2_1_1"]
    // H8Vy9zYKUnru74Z5RNzFtrgHWqg-7t5v5zBVAXeQhYM
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_2_1_1 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8zXzJfMV8xIiwgInZhbHVlX29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMl8zXzJfMV8xIl0"

    // ["salt_object_partly_disclosed_1_3", "key_object_partly_disclosed_1_3", [{"...":"lPIVfEM9o8ze5vXB4zoEXOabTAZyAWX_KcSp6GwjBN0"}, {"...":"decoyv1IB2MeMzj1vn6lUhSTTFWSLmp867EGner_Lu8"}, {"...":"QSzUsv1IB2MeMzj1vn6lUhSTTFWSLmp867EGner_Lu8"}]]
    // raXs5cEG0_2uWLK5PIW0N5I8rrzXk7H3tgKBBOMoXD8
    private const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_3 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfMyIsICJrZXlfb2JqZWN0X3BhcnRseV9kaXNjbG9zZWRfMV8zIiwgW3siLi4uIjoibFBJVmZFTTlvOHplNXZYQjR6b0VYT2FiVEFaeUFXWF9LY1NwNkd3akJOMCJ9LCB7Ii4uLiI6ImRlY295djFJQjJNZU16ajF2bjZsVWhTVFRGV1NMbXA4NjdFR25lcl9MdTgifSwgeyIuLi4iOiJRU3pVc3YxSUIyTWVNemoxdm42bFVoU1RURldTTG1wODY3RUduZXJfTHU4In1dXQ"

    // ["salt_object_partly_disclosed_1_3_1", "value_object_partly_disclosed_1_3_1"]
    // lPIVfEM9o8ze5vXB4zoEXOabTAZyAWX_KcSp6GwjBN0
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_3_1 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfM18xIiwgInZhbHVlX29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfM18xIl0"

    // ["salt_object_partly_disclosed_1_3_2", "value_object_partly_disclosed_1_3_2"]
    // QSzUsv1IB2MeMzj1vn6lUhSTTFWSLmp867EGner_Lu8
    const val OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_3_2 =
        "WyJzYWx0X29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfM18yIiwgInZhbHVlX29iamVjdF9wYXJ0bHlfZGlzY2xvc2VkXzFfM18yIl0"

    const val JSON = """
{
   "key_undisclosed":"value_undisclosed",
   "key_flat":"value_flat",
   "key_flat_array":[
      "value_flat_array_1",
      "value_flat_array_2"
   ],
   "key_flat_object":{
      "key_nested_object_1":"value_flat_object_1",
      "key_nested_object_2":"value_flat_object_2"
   },
   "key_array_undisclosed":[
      "value_array_undisclosed_1",
      "value_array_undisclosed_2"
   ],
   "key_array_partly_disclosed":[
      "value_array_partly_disclosed_1",
      "value_array_partly_disclosed_2",
      "value_array_partly_disclosed_3"
   ],
   "key_object_undisclosed":{
      "key_nested_object_1":"value_object_undisclosed_1",
      "key_nested_object_2":"value_object_undisclosed_2"
   },
   "key_object_partly_disclosed":{
      "key_object_partly_disclosed_1_1":"value_object_partly_disclosed_1_1",
      "key_object_partly_disclosed_1_2":{
         "key_object_partly_disclosed_1_2_1":"value_object_partly_disclosed_1_2_1",
         "key_object_partly_disclosed_1_2_2":"value_object_partly_disclosed_1_2_2",
         "key_object_partly_disclosed_1_2_3":[
            {
               "key_object_partly_disclosed_1_2_3_1":[
                  "value_object_partly_disclosed_1_2_3_1_1_1"
               ]
            },
            {
               "key_object_partly_disclosed_1_2_3_1":[
                  "value_object_partly_disclosed_1_2_3_2_1_1"
               ]
            }
         ]
      },
      "key_object_partly_disclosed_1_3":[
         "value_object_partly_disclosed_1_3_1",
         "value_object_partly_disclosed_1_3_2"
      ]
   }
}
    """

    const val JWT =
        "eyJ0eXAiOiJjb21wbGV4IiwiYWxnIjoiRVM1MTIifQ.eyJrZXlfdW5kaXNjbG9zZWQiOiJ2YWx1ZV91bmRpc2Nsb3NlZCIsIl9zZCI6WyJwc2Z2ZDdCMmpiZk44X3Rma1JtZW4xOVhURUt5b1Axak85REhldklHMEl3IiwiZGVjb3ltNTJqd1U2OHVGcUNFQV95bUZBbjNnUGVuZDcwbUVabkxJaFJfcyIsIjhYdG92bTUyandVNjh1RnFDRUFfeW1GQW4zZ1BlbmQ3MG1FWm5MSWhSX3MiLCJtQ2pfa1VjUXhORnloVDRCRmVQUGFRWjNYMTQxUE1Sd0hMQjVicldva19jIiwiZGVjb3lrMDJ0UmNCOGY0d3U3QkdaMlFxN01uUFVQOW4xLXlMRHF2S1kwZyJdLCJrZXlfYXJyYXlfdW5kaXNjbG9zZWQiOlsidmFsdWVfYXJyYXlfdW5kaXNjbG9zZWRfMSIsInZhbHVlX2FycmF5X3VuZGlzY2xvc2VkXzIiXSwia2V5X2FycmF5X3BhcnRseV9kaXNjbG9zZWQiOlt7Ii4uLiI6ImtKSzRjVmZnVlRfeUl0TnNOZTM5bFg3VHdhSUhhSEM5QmF0c3pDWHQyd3cifSwidmFsdWVfYXJyYXlfcGFydGx5X2Rpc2Nsb3NlZF8yIix7Ii4uLiI6ImRlY295VmZnVlRfeUl0TnNOZTM5bFg3VHdhSUhhSEM5QmF0c3pDWHQyd3cifSx7Ii4uLiI6IjBONmo1RjR4X1JFOXltbXRYa1BxTUVSYnNPU1ZBQkpfTlR2dU9LVWd4bmMifV0sImtleV9vYmplY3RfdW5kaXNjbG9zZWQiOnsia2V5X25lc3RlZF9vYmplY3RfMSI6InZhbHVlX29iamVjdF91bmRpc2Nsb3NlZF8xIiwia2V5X25lc3RlZF9vYmplY3RfMiI6InZhbHVlX29iamVjdF91bmRpc2Nsb3NlZF8yIn0sImtleV9vYmplY3RfcGFydGx5X2Rpc2Nsb3NlZCI6eyJrZXlfb2JqZWN0X3BhcnRseV9kaXNjbG9zZWRfMV8xIjoidmFsdWVfb2JqZWN0X3BhcnRseV9kaXNjbG9zZWRfMV8xIiwiX3NkIjpbImRlY295TERwYUNjXzF5NEM1eko1OE1VRDZKNG9wY2dPVTdQZUZKaWlWMjgiLCJTRVdTTHhtZlRwRlZHcm05RGJ4cE5rM2VwTGRpdlN2MlRGU1dnb0JuR19rIiwicmFYczVjRUcwXzJ1V0xLNVBJVzBONUk4cnJ6WGs3SDN0Z0tCQk9Nb1hEOCIsImRlY295S3FwN3pBd1UwTGFjWU10Q0pJQ0RXdlU4aEJfbXBtRXBSNGEzRWciXX0sIl9zZF9hbGciOiJzaGEtMjU2In0.AEeBP94Nhy9Zdw6zCrz78jqMLKskTWCaLkXbfRF2q8hslURfDv0JLEBjip36toCccP3JrLL694fBouCaVxNbnN44AfTfAEp8j_gKlSMuMTZk7wOfDOdWOcs9c0HqG4t49LRK9uRLArK1i-r5wKtgA7dfwNemgyku0mFDjJkzYisB1nZ1"
    val SD_JWT = JWT + listOf(
        FLAT_DISCLOSURE,
        FLAT_ARRAY_DISCLOSURE,
        FLAT_OBJECT_DISCLOSURE,
        ARRAY_PARTLY_DISCLOSED_DISCLOSURE_1,
        ARRAY_PARTLY_DISCLOSED_DISCLOSURE_3,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_1,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_1,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_2,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_2_1,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_2_3_2_1_1,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_3,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_3_1,
        OBJECT_PARTLY_DISCLOSED_DISCLOSURE_1_3_2,
    ).shuffled().toDisclosures()
}
