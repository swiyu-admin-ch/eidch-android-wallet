package ch.admin.foitt.openid4vc.util

fun CharSequence.splitNoEmpty(delimiter: String) = this.split(delimiter).mapNotNull { it.ifEmpty { null } }
