import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProfile, updateProfile } from '../api/member'
import Button from '../components/common/Button'
import type { Gender, DrinkingType, SmokingType } from '../types'

const MBTI_OPTIONS = ['ENFP','ENFJ','ENTP','ENTJ','ESFP','ESFJ','ESTP','ESTJ',
                      'INFP','INFJ','INTP','INTJ','ISFP','ISFJ','ISTP','ISTJ']

const ProfileEditPage = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: profile, isLoading } = useQuery({
    queryKey: ['myProfile'],
    queryFn: getProfile,
  })

  const [form, setForm] = useState({
    name: '', age: '', gender: '' as Gender | '',
    job: '', height: '', mbti: '', hobby: '',
    drinking: '' as DrinkingType | '', smoking: '' as SmokingType | '',
    religion: '', idealType: '', introduction: '',
  })

  useEffect(() => {
    if (profile) {
      setForm({
        name: profile.name,
        age: String(profile.age),
        gender: profile.gender,
        job: profile.job,
        height: String(profile.height),
        mbti: profile.mbti,
        hobby: profile.hobby,
        drinking: profile.drinking,
        smoking: profile.smoking,
        religion: profile.religion,
        idealType: profile.idealType,
        introduction: profile.introduction,
      })
    }
  }, [profile])

  const mutation = useMutation({
    mutationFn: () => updateProfile({
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
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myProfile'] })
      navigate('/mypage')
    },
    onError: () => alert('저장에 실패했습니다.'),
  })

  const update = (key: keyof typeof form, value: string) =>
    setForm((p) => ({ ...p, [key]: value }))

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
  }

  const field = (label: string, content: React.ReactNode) => (
    <div style={{ marginBottom: 14 }}>
      <label style={labelStyle}>{label}</label>
      {content}
    </div>
  )

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 60 }}>
        <p style={{ color: 'var(--text-light)' }}>불러오는 중...</p>
      </div>
    )
  }

  return (
    <div style={{ padding: '20px 16px' }}>
      <button
        onClick={() => navigate(-1)}
        style={{
          background: 'none',
          border: 'none',
          fontSize: 14,
          color: 'var(--text-light)',
          cursor: 'pointer',
          fontFamily: 'inherit',
          marginBottom: 16,
        }}
      >
        ← 뒤로
      </button>

      <h1
        style={{
          fontSize: 22,
          fontWeight: 800,
          letterSpacing: '-0.5px',
          background: 'var(--gradient)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          marginBottom: 20,
        }}
      >
        프로필 수정
      </h1>

      <div
        style={{
          background: '#fff',
          borderRadius: 'var(--radius)',
          padding: 20,
          boxShadow: 'var(--shadow)',
        }}
      >
        {field('이름', <input style={inputStyle} value={form.name} onChange={(e) => update('name', e.target.value)} />)}
        {field('나이', <input style={inputStyle} type="number" value={form.age} onChange={(e) => update('age', e.target.value)} />)}
        {field('성별', (
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
        {field('직업', <input style={inputStyle} value={form.job} onChange={(e) => update('job', e.target.value)} />)}
        {field('키 (cm)', <input style={inputStyle} type="number" value={form.height} onChange={(e) => update('height', e.target.value)} />)}
        {field('MBTI', (
          <select style={inputStyle} value={form.mbti} onChange={(e) => update('mbti', e.target.value)}>
            <option value="">선택</option>
            {MBTI_OPTIONS.map((m) => <option key={m} value={m}>{m}</option>)}
          </select>
        ))}
        {field('취미', <input style={inputStyle} value={form.hobby} onChange={(e) => update('hobby', e.target.value)} />)}
        {field('음주', (
          <select style={inputStyle} value={form.drinking} onChange={(e) => update('drinking', e.target.value)}>
            <option value="">선택</option>
            <option value="NONE">음주 안함</option>
            <option value="SOMETIMES">가끔 마심</option>
            <option value="OFTEN">자주 마심</option>
          </select>
        ))}
        {field('흡연', (
          <select style={inputStyle} value={form.smoking} onChange={(e) => update('smoking', e.target.value)}>
            <option value="">선택</option>
            <option value="NONE">비흡연</option>
            <option value="SOMETIMES">가끔 피움</option>
            <option value="OFTEN">흡연</option>
          </select>
        ))}
        {field('종교', <input style={inputStyle} value={form.religion} onChange={(e) => update('religion', e.target.value)} />)}
        {field('이상형', (
          <textarea
            style={{ ...inputStyle, height: 80, resize: 'none' }}
            value={form.idealType}
            onChange={(e) => update('idealType', e.target.value)}
          />
        ))}
        {field('자기소개', (
          <textarea
            style={{ ...inputStyle, height: 100, resize: 'none' }}
            value={form.introduction}
            onChange={(e) => update('introduction', e.target.value)}
          />
        ))}

        <div style={{ marginTop: 8 }}>
          <Button fullWidth onClick={() => mutation.mutate()} disabled={mutation.isPending}>
            {mutation.isPending ? '저장 중...' : '저장하기'}
          </Button>
        </div>
      </div>
    </div>
  )
}

export default ProfileEditPage
