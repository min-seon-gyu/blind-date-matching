package com.blinddate.bar.repository

import com.blinddate.bar.entity.Bar
import org.springframework.data.jpa.repository.JpaRepository

interface BarRepository : JpaRepository<Bar, Long>
