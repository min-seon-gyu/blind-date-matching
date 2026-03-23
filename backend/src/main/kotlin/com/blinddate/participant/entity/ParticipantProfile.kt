package com.blinddate.participant.entity

import com.blinddate.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "participant_profile")
class ParticipantProfile(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", unique = true, nullable = false)
    val participant: Participant,

    @Column(nullable = false) var name: String,
    @Column(nullable = false) var age: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var gender: Gender,
    @Column(nullable = false) var job: String,
    @Column var height: Int = 0,
    @Column(length = 4) var mbti: String = "",
    @Column(columnDefinition = "TEXT") var hobby: String = "",
    @Enumerated(EnumType.STRING) var drinking: DrinkingType = DrinkingType.NONE,
    @Enumerated(EnumType.STRING) var smoking: SmokingType = SmokingType.NONE,
    @Column var religion: String = "",
    @Column(columnDefinition = "TEXT") var idealType: String = "",
    @Column(columnDefinition = "TEXT") var introduction: String = "",
    @Column var photoUrl: String = ""
) : BaseEntity()
