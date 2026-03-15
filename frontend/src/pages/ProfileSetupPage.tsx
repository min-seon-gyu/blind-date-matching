import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { createProfile } from '../api/member'
import Button from '../components/common/Button'
import type { Gender, DrinkingType, SmokingType } from '../types'

interface ProfileForm {
  name: string
  age: string
  gender: Gender | ''
  job: string
  height: string
  mbti: string
  hobby: string
  drinking: DrinkingType | ''
  smoking: SmokingType | ''
  religion: string
  idealType: string
  introduction: string
}

const MBTI_OPTIONS = ['ENFP','ENFJ','ENTP','ENTJ','ESFP','ESFJ','ESTP','ESTJ',
                      'INFP','INFJ','INTP','INTJ','ISFP','ISFJ','ISTP','ISTJ']

const ProfileSetupPage = () => {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [form, setForm] = useState<ProfileForm>({
    name: '', age: '', gender: '', job: '', height: '', mbti: '',
    hobby: '', drinking: '', smoking: '', religion: '', idealType: '', introduction: '',
  })

  const mutation = useMutation({
    mutationFn: () => createProfile({
      name: form.name,
      age: parseInt(form.age),
      gender: form.gender as Gender,
      job: form.job,
      height: parseInt(form.height),
      mbti: form.mbti,
      hobby: form.hobby,
      drinking: form.drinking as DrinkingType,
      smoking: form.smoking as SmokingType,
      religion: form.religion,
      idealType: form.idealType,
      introduction: form.introduction,
    }),
    onSuccess: () => navigate('/'),
  })

  const update = (key: keyof ProfileForm, value: string) =>
    setForm((prev) => ({ ...prev, [key]: value }))

  const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '12px 14px',
    border: '1.5px solid var(--border)',
    borderRadius: 10,
    fontSize: 15,
    fontFamily: 'inherit',
    outline: 'none',
    background: '#fff',
    color: 'var(--text)',
    marginTop: 6,
  }

  const labelStyle: React.CSSProperties = {
    fontSize: 13,
    fontWeight: 600,
    color: 'var(--text-light)',
    letterSpacing: '-0.2px',
  }

  const fieldGroup = (label: string, content: React.ReactNode) => (
    <div style={{ marginBottom: 16 }}>
      <label style={labelStyle}>{label}</label>
      {content}
    </div>
  )

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'var(--bg)',
        padding: '0 0 40px',
      }}
    >
      {/* Header */}
      <div
        style={{
          background: 'var(--gradient)',
          padding: '40px 24px 24px',
          color: '#fff',
        }}
      >
        <p style={{ fontSize: 13, opacity: 0.85, marginBottom: 4 }}>단계 {step} / 2</p>
        <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: '-0.5px' }}>
          {step === 1 ? '기본 정보를 입력해주세요' : '추가 정보를 입력해주세요'}
        </h1>
        {/* Progress bar */}
        <div
          style={{
            marginTop: 16,
            height: 4,
            background: 'rgba(255,255,255,0.3)',
            borderRadius: 2,
          }}
        >
          <div
            style={{
              height: '100%',
              width: step === 1 ? '50%' : '100%',
              background: '#fff',
              borderRadius: 2,
              transition: 'width 0.3s',
            }}
          />
        </div>
      </div>

      <div style={{ padding: '24px 20px' }}>
        {step === 1 && (
          <div
            style={{
              background: '#fff',
              borderRadius: 'var(--radius)',
              padding: 20,
              boxShadow: 'var(--shadow)',
            }}
          >
            {fieldGroup('이름', (
              <input
                style={inputStyle}
                placeholder="이름을 입력하세요"
                value={form.name}
                onChange={(e) => update('name', e.target.value)}
              />
            ))}
            {fieldGroup('나이', (
              <input
                style={inputStyle}
                type="number"
                placeholder="나이를 입력하세요"
                value={form.age}
                onChange={(e) => update('age', e.target.value)}
              />
            ))}
            {fieldGroup('성별', (
              <div style={{ display: 'flex', gap: 10, marginTop: 6 }}>
                {(['MALE', 'FEMALE'] as Gender[]).map((g) => (
                  <button
                    key={g}
                    type="button"
                    onClick={() => update('gender', g)}
                    style={{
                      flex: 1,
                      padding: '10px',
                      border: `2px solid ${form.gender === g ? 'var(--primary)' : 'var(--border)'}`,
                      borderRadius: 10,
                      background: form.gender === g ? '#fff0f4' : '#fff',
                      cursor: 'pointer',
                      fontWeight: 600,
                      fontSize: 14,
                      color: form.gender === g ? 'var(--primary)' : 'var(--text-light)',
                      fontFamily: 'inherit',
                    }}
                  >
                    {g === 'MALE' ? '👨 남성' : '👩 여성'}
                  </button>
                ))}
              </div>
            ))}
            {fieldGroup('직업', (
              <input
                style={inputStyle}
                placeholder="직업을 입력하세요"
                value={form.job}
                onChange={(e) => update('job', e.target.value)}
              />
            ))}
            {fieldGroup('키 (cm)', (
              <input
                style={inputStyle}
                type="number"
                placeholder="키를 입력하세요"
                value={form.height}
                onChange={(e) => update('height', e.target.value)}
              />
            ))}
            {fieldGroup('MBTI', (
              <select
                style={inputStyle}
                value={form.mbti}
                onChange={(e) => update('mbti', e.target.value)}
              >
                <option value="">MBTI를 선택하세요</option>
                {MBTI_OPTIONS.map((m) => (
                  <option key={m} value={m}>{m}</option>
                ))}
              </select>
            ))}

            <div style={{ marginTop: 8 }}>
              <Button
                fullWidth
                onClick={() => setStep(2)}
                disabled={!form.name || !form.age || !form.gender || !form.job || !form.height}
              >
                다음 단계
              </Button>
            </div>
          </div>
        )}

        {step === 2 && (
          <div
            style={{
              background: '#fff',
              borderRadius: 'var(--radius)',
              padding: 20,
              boxShadow: 'var(--shadow)',
            }}
          >
            {fieldGroup('취미', (
              <input
                style={inputStyle}
                placeholder="취미를 입력하세요"
                value={form.hobby}
                onChange={(e) => update('hobby', e.target.value)}
              />
            ))}
            {fieldGroup('음주', (
              <select
                style={inputStyle}
                value={form.drinking}
                onChange={(e) => update('drinking', e.target.value)}
              >
                <option value="">음주 빈도를 선택하세요</option>
                <option value="NONE">음주 안함</option>
                <option value="SOMETIMES">가끔 마심</option>
                <option value="OFTEN">자주 마심</option>
              </select>
            ))}
            {fieldGroup('흡연', (
              <select
                style={inputStyle}
                value={form.smoking}
                onChange={(e) => update('smoking', e.target.value)}
              >
                <option value="">흡연 여부를 선택하세요</option>
                <option value="NONE">비흡연</option>
                <option value="SOMETIMES">가끔 피움</option>
                <option value="OFTEN">흡연</option>
              </select>
            ))}
            {fieldGroup('종교', (
              <input
                style={inputStyle}
                placeholder="종교를 입력하세요 (없으면 '없음')"
                value={form.religion}
                onChange={(e) => update('religion', e.target.value)}
              />
            ))}
            {fieldGroup('이상형', (
              <textarea
                style={{ ...inputStyle, height: 80, resize: 'none' }}
                placeholder="이상형을 자유롭게 작성해주세요"
                value={form.idealType}
                onChange={(e) => update('idealType', e.target.value)}
              />
            ))}
            {fieldGroup('자기소개', (
              <textarea
                style={{ ...inputStyle, height: 100, resize: 'none' }}
                placeholder="자신을 소개해주세요"
                value={form.introduction}
                onChange={(e) => update('introduction', e.target.value)}
              />
            ))}

            <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
              <button
                type="button"
                onClick={() => setStep(1)}
                style={{
                  flex: 1,
                  padding: '12px',
                  border: '2px solid var(--border)',
                  borderRadius: 12,
                  background: '#fff',
                  fontSize: 15,
                  fontWeight: 600,
                  cursor: 'pointer',
                  fontFamily: 'inherit',
                  color: 'var(--text-light)',
                }}
              >
                이전
              </button>
              <div style={{ flex: 2 }}>
                <Button
                  fullWidth
                  onClick={() => mutation.mutate()}
                  disabled={mutation.isPending || !form.drinking || !form.smoking}
                >
                  {mutation.isPending ? '저장 중...' : '프로필 완성하기 🎉'}
                </Button>
              </div>
            </div>

            {mutation.isError && (
              <p style={{ color: '#ff4d4f', fontSize: 13, textAlign: 'center', marginTop: 10 }}>
                오류가 발생했습니다. 다시 시도해주세요.
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default ProfileSetupPage
