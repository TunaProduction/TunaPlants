package com.tunacreations.tunaplants.core.data.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Inject
import jakarta.inject.Provider

class HiltWorkerFactory @Inject constructor(
    private val workerFactories: @JvmSuppressWildcards Map<Class<out ListenableWorker>, Provider<WorkerFactory>>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val foundEntry = workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }
        val factoryProvider = foundEntry?.value ?: return null
        return factoryProvider.get().createWorker(appContext, workerClassName, workerParameters)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface WorkerBindingModule {
    @Binds
    fun bindWorkerFactory(factory: HiltWorkerFactory): WorkerFactory
}