package com.chat.aoita.di

import com.chat.aoita.data.provider.ChatProvider
import com.chat.aoita.data.provider.DemoProvider
import com.chat.aoita.data.repository.ChatRepository
import com.chat.aoita.data.repository.ChatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds abstract fun repository(impl: ChatRepositoryImpl): ChatRepository

    /** 接入 Anthropic / OpenAI 时，把这里换成对应实现（或按设置选择）。 */
    @Binds abstract fun provider(impl: DemoProvider): ChatProvider
}
