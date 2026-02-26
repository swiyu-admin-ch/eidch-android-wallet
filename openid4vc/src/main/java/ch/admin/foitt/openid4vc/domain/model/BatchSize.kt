package ch.admin.foitt.openid4vc.domain.model

typealias BatchSize = Int

val BatchSize.threshold: Int
    get() = (this * 0.2f).toInt()
