package co.edu.unal.petbuddy.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// This module is currently empty because all dependencies are provided via
// constructor injection. It can be used later to provide dependencies
// for interfaces or classes from external libraries.
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
}
