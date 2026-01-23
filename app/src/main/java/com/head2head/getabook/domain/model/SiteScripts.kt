package com.head2head.getabook.domain.model

data class SiteScripts(
    val earlyCss: String?,   // выполняется в onPageStarted
    val fullJs: String?      // выполняется в onPageFinished
)
