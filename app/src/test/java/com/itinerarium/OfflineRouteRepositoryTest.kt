package com.itinerarium

import com.itinerarium.data.model.TransportMode
import com.itinerarium.data.repository.MockRouteRepository
import com.itinerarium.data.repository.OfflineRouteRepositoryImpl
import com.itinerarium.data.repository.RepositoryProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

import com.itinerarium.data.repository.RemoteRouteRepositoryImpl

class OfflineRouteRepositoryTest {

    @Test
    fun offlineRouteRepository_fallbackToMock_whenGraphNotAvailable() = runTest {
        val mockRepo = MockRouteRepository()
        val repository = OfflineRouteRepositoryImpl(
            context = null,
            engineManager = null,
            fallbackRepository = mockRepo
        )

        val result = repository.calculateRoutes(
            origin = "Praça da Sé",
            destination = "Avenida Paulista",
            selectedMode = TransportMode.CAR
        )

        assertTrue(result.isSuccess)
        val routes = result.getOrNull()
        assertNotNull(routes)
        assertTrue(routes!!.isNotEmpty())
    }

    @Test
    fun repositoryProvider_returnsRemoteRouteRepositoryImpl() {
        RepositoryProvider.USE_MOCK = false
        val repo = RepositoryProvider.getRouteRepository()
        assertTrue(repo is RemoteRouteRepositoryImpl)
    }

    @Test
    fun repositoryProvider_useMockTrue_returnsMockRouteRepository() {
        RepositoryProvider.USE_MOCK = true
        val repo = RepositoryProvider.getRouteRepository()
        assertTrue(repo is MockRouteRepository)
    }
}
