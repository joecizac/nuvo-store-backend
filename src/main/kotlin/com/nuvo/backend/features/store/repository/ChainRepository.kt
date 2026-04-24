package com.nuvo.backend.features.store.repository

import com.nuvo.backend.features.store.domain.Chain
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ChainRepository : JpaRepository<Chain, UUID>
