import { useQuery } from '@tanstack/react-query'
import { getEventsByMonth, getEvent } from '../api/event'

export const useEventsByMonth = (year: number, month: number) =>
  useQuery({
    queryKey: ['events', year, month],
    queryFn: () => getEventsByMonth(year, month),
  })

export const useEvent = (id: number) =>
  useQuery({
    queryKey: ['event', id],
    queryFn: () => getEvent(id),
    enabled: !!id,
  })
