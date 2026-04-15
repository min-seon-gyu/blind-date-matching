import { useEffect, useRef } from 'react'
import { MapPin } from 'lucide-react'

interface KakaoMapProps {
  latitude: number | null
  longitude: number | null
  name: string
}

declare global {
  interface Window {
    kakao?: {
      maps: {
        load: (callback: () => void) => void
        LatLng: new (lat: number, lng: number) => unknown
        Map: new (container: HTMLElement, options: { center: unknown; level: number }) => unknown
        Marker: new (options: { map: unknown; position: unknown }) => unknown
        InfoWindow: new (options: { content: string }) => {
          open: (map: unknown, marker: unknown) => void
        }
      }
    }
  }
}

export default function KakaoMap({ latitude, longitude, name }: KakaoMapProps) {
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!latitude || !longitude || !containerRef.current) return

    const loadMap = () => {
      if (!window.kakao?.maps || !containerRef.current) return

      window.kakao.maps.load(() => {
        const center = new window.kakao!.maps.LatLng(latitude, longitude)
        const map = new window.kakao!.maps.Map(containerRef.current!, {
          center,
          level: 3,
        })

        const marker = new window.kakao!.maps.Marker({
          map,
          position: center,
        })

        const infoWindow = new window.kakao!.maps.InfoWindow({
          content: `<div style="padding:4px 8px;font-size:12px;white-space:nowrap;">${name}</div>`,
        })
        infoWindow.open(map, marker)
      })
    }

    // Check if Kakao Maps SDK is already loaded
    if (window.kakao?.maps) {
      loadMap()
      return
    }

    // Load Kakao Maps SDK
    const apiKey = import.meta.env.VITE_KAKAO_MAP_API_KEY
    if (!apiKey) return

    const script = document.createElement('script')
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false`
    script.async = true
    script.onload = loadMap
    document.head.appendChild(script)

    return () => {
      // Cleanup: don't remove script as it may be used by other components
    }
  }, [latitude, longitude, name])

  if (!latitude || !longitude) {
    return (
      <div className="flex items-center gap-2 p-4 bg-gray-50 rounded-xl text-gray-500 text-sm">
        <MapPin className="w-4 h-4" />
        <span>위치 정보가 없습니다</span>
      </div>
    )
  }

  return (
    <div
      ref={containerRef}
      className="w-full h-48 rounded-xl overflow-hidden bg-gray-100"
    />
  )
}
