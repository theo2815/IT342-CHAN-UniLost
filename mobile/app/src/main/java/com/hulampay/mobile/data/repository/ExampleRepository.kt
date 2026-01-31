package com.hulampay.mobile.data.repository

import com.hulampay.mobile.data.dao.ExampleDao
import com.hulampay.mobile.data.remote.AppApi
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ExampleRepository @Inject constructor(private val appApi: AppApi, private val exampleDao: ExampleDao)
