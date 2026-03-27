import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProfile, updateProfile } from '@/api/participant'
import { uploadFile } from '@/api/upload'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ArrowLeft, Camera, Loader2 } from 'lucide-react'
import type { DrinkingType, SmokingType } from '@/types'

const MBTI_OPTIONS = [
  'INTJ', 'INTP', 'ENTJ', 'ENTP',
  'INFJ', 'INFP', 'ENFJ', 'ENFP',
  'ISTJ', 'ISFJ', 'ESTJ', 'ESFJ',
  'ISTP', 'ISFP', 'ESTP', 'ESFP',
]

const DRINKING_OPTIONS: { value: DrinkingType; label: string }[] = [
  { value: 'NONE', label: '안 마심' },
  { value: 'SOMETIMES', label: '가끔' },
  { value: 'OFTEN', label: '자주' },
]

const SMOKING_OPTIONS: { value: SmokingType; label: string }[] = [
  { value: 'NONE', label: '비흡연' },
  { value: 'SOMETIMES', label: '가끔' },
  { value: 'OFTEN', label: '자주' },
]

export default function ProfileEditPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: profile, isLoading } = useQuery({
    queryKey: ['profile'],
    queryFn: getProfile,
  })

  const [name, setName] = useState('')
  const [age, setAge] = useState('')
  const [job, setJob] = useState('')
  const [height, setHeight] = useState('')
  const [mbti, setMbti] = useState('')
  const [hobby, setHobby] = useState('')
  const [drinking, setDrinking] = useState<DrinkingType>('NONE')
  const [smoking, setSmoking] = useState<SmokingType>('NONE')
  const [religion, setReligion] = useState('')
  const [idealType, setIdealType] = useState('')
  const [introduction, setIntroduction] = useState('')
  const [photoUrl, setPhotoUrl] = useState('')
  const [uploading, setUploading] = useState(false)

  useEffect(() => {
    if (profile) {
      setName(profile.name)
      setAge(String(profile.age))
      setJob(profile.job)
      setHeight(String(profile.height))
      setMbti(profile.mbti)
      setHobby(profile.hobby || '')
      setDrinking(profile.drinking)
      setSmoking(profile.smoking)
      setReligion(profile.religion || '')
      setIdealType(profile.idealType || '')
      setIntroduction(profile.introduction || '')
      setPhotoUrl(profile.photoUrl || '')
    }
  }, [profile])

  const mutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile'] })
      navigate('/me', { replace: true })
    },
  })

  const handlePhotoUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const url = await uploadFile(file)
      setPhotoUrl(url)
    } catch {
      alert('사진 업로드에 실패했습니다.')
    } finally {
      setUploading(false)
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    mutation.mutate({
      name,
      age: Number(age),
      gender: profile!.gender,
      job,
      height: Number(height),
      mbti,
      hobby: hobby || null,
      drinking,
      smoking,
      religion: religion || null,
      idealType: idealType || null,
      introduction: introduction || null,
      photoUrl: photoUrl || null,
    })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 text-[#FF6B6B] animate-spin" />
      </div>
    )
  }

  const isValid = name && age && job && height && mbti

  return (
    <div className="min-h-screen bg-[#FFF5F5]">
      {/* Header */}
      <div className="sticky top-0 z-10 bg-[#FFF5F5] px-4 py-3 flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="w-10 h-10 flex items-center justify-center rounded-xl hover:bg-white/60 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-lg font-bold">프로필 수정</h1>
      </div>

      <form onSubmit={handleSubmit} className="px-4 pb-8 space-y-6">
        {/* Photo Upload */}
        <div className="flex flex-col items-center gap-2">
          <label className="relative cursor-pointer">
            <div className="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border-2 border-dashed border-gray-300">
              {photoUrl ? (
                <img src={photoUrl} alt="프로필" className="w-full h-full object-cover" />
              ) : uploading ? (
                <Loader2 className="w-6 h-6 text-gray-400 animate-spin" />
              ) : (
                <Camera className="w-6 h-6 text-gray-400" />
              )}
            </div>
            <input
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handlePhotoUpload}
            />
          </label>
          <span className="text-xs text-gray-500">프로필 사진</span>
        </div>

        {/* Name */}
        <div className="space-y-2">
          <Label htmlFor="name">이름</Label>
          <Input
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="이름을 입력하세요"
            className="h-11 rounded-xl"
          />
        </div>

        {/* Age */}
        <div className="space-y-2">
          <Label htmlFor="age">나이</Label>
          <Input
            id="age"
            type="number"
            value={age}
            onChange={(e) => setAge(e.target.value)}
            placeholder="나이를 입력하세요"
            className="h-11 rounded-xl"
            min={20}
            max={60}
          />
        </div>

        {/* Gender - display only */}
        <div className="space-y-2">
          <Label>성별</Label>
          <div className="h-11 rounded-xl bg-gray-100 flex items-center px-3 text-gray-500">
            {profile?.gender === 'MALE' ? '남성' : '여성'}
          </div>
        </div>

        {/* Job */}
        <div className="space-y-2">
          <Label htmlFor="job">직업</Label>
          <Input
            id="job"
            value={job}
            onChange={(e) => setJob(e.target.value)}
            placeholder="직업을 입력하세요"
            className="h-11 rounded-xl"
          />
        </div>

        {/* Height */}
        <div className="space-y-2">
          <Label htmlFor="height">키 (cm)</Label>
          <Input
            id="height"
            type="number"
            value={height}
            onChange={(e) => setHeight(e.target.value)}
            placeholder="키를 입력하세요"
            className="h-11 rounded-xl"
            min={140}
            max={210}
          />
        </div>

        {/* MBTI */}
        <div className="space-y-2">
          <Label>MBTI</Label>
          <div className="grid grid-cols-4 gap-2">
            {MBTI_OPTIONS.map((option) => (
              <button
                key={option}
                type="button"
                onClick={() => setMbti(option)}
                className={`h-10 rounded-lg text-sm font-medium transition-all ${
                  mbti === option
                    ? 'bg-[#FF6B6B] text-white shadow-sm'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                {option}
              </button>
            ))}
          </div>
        </div>

        {/* Hobby */}
        <div className="space-y-2">
          <Label htmlFor="hobby">취미</Label>
          <Input
            id="hobby"
            value={hobby}
            onChange={(e) => setHobby(e.target.value)}
            placeholder="취미를 입력하세요"
            className="h-11 rounded-xl"
          />
        </div>

        {/* Drinking */}
        <div className="space-y-2">
          <Label>음주</Label>
          <div className="grid grid-cols-3 gap-2">
            {DRINKING_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => setDrinking(option.value)}
                className={`h-10 rounded-xl text-sm font-medium transition-all ${
                  drinking === option.value
                    ? 'bg-[#FF6B6B] text-white shadow-sm'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        {/* Smoking */}
        <div className="space-y-2">
          <Label>흡연</Label>
          <div className="grid grid-cols-3 gap-2">
            {SMOKING_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => setSmoking(option.value)}
                className={`h-10 rounded-xl text-sm font-medium transition-all ${
                  smoking === option.value
                    ? 'bg-[#FF6B6B] text-white shadow-sm'
                    : 'bg-white text-gray-600 border border-gray-200'
                }`}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        {/* Religion */}
        <div className="space-y-2">
          <Label htmlFor="religion">종교</Label>
          <Input
            id="religion"
            value={religion}
            onChange={(e) => setReligion(e.target.value)}
            placeholder="종교를 입력하세요"
            className="h-11 rounded-xl"
          />
        </div>

        {/* Ideal Type */}
        <div className="space-y-2">
          <Label htmlFor="idealType">이상형</Label>
          <textarea
            id="idealType"
            value={idealType}
            onChange={(e) => setIdealType(e.target.value)}
            placeholder="이상형을 입력하세요"
            className="w-full h-20 rounded-xl border border-input bg-transparent px-3 py-2 text-base transition-colors placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 resize-none"
          />
        </div>

        {/* Introduction */}
        <div className="space-y-2">
          <Label htmlFor="introduction">자기소개</Label>
          <textarea
            id="introduction"
            value={introduction}
            onChange={(e) => setIntroduction(e.target.value)}
            placeholder="간단한 자기소개를 작성하세요"
            className="w-full h-24 rounded-xl border border-input bg-transparent px-3 py-2 text-base transition-colors placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 resize-none"
          />
        </div>

        {/* Submit Button */}
        <Button
          type="submit"
          disabled={!isValid || mutation.isPending}
          className="w-full h-12 rounded-xl bg-[#FF6B6B] hover:bg-[#FF5252] text-white font-semibold text-base"
        >
          {mutation.isPending ? (
            <Loader2 className="w-5 h-5 animate-spin" />
          ) : (
            '프로필 수정'
          )}
        </Button>

        {mutation.isError && (
          <p className="text-center text-sm text-red-500">
            프로필 수정에 실패했습니다. 다시 시도해주세요.
          </p>
        )}
      </form>
    </div>
  )
}
