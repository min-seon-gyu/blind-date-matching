import client from './client'
import axios from 'axios'

interface PresignedUrlResponse {
  uploadUrl: string
  fileUrl: string
}

export async function getPresignedUrl(
  contentType: string,
  directory?: string
): Promise<PresignedUrlResponse> {
  const res = await client.post<PresignedUrlResponse>('/upload/presigned-url', {
    contentType,
    directory,
  })
  return res.data
}

export async function uploadFile(file: File): Promise<string> {
  const { uploadUrl, fileUrl } = await getPresignedUrl(file.type, 'profiles')

  await axios.put(uploadUrl, file, {
    headers: { 'Content-Type': file.type },
  })

  return fileUrl
}
