import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";

const api = axios.create({ baseURL: "/api/v1" });

export interface Flag {
  id: string;
  key: string;
  name: string;
  type: "BOOLEAN" | "MULTIVARIATE" | "PERCENTAGE";
  enabled: boolean;
  version: number;
  updatedAt: string;
}

interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export function useFlags(projectId: string, environmentId: string, search = "") {
  return useQuery({
    queryKey: ["flags", projectId, environmentId, search],
    queryFn: async () => {
      const { data } = await api.get<Page<Flag>>(
        `/projects/${projectId}/environments/${environmentId}/flags`,
        { params: { search } }
      );
      return data;
    },
  });
}

export function useToggleFlag(projectId: string, environmentId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ flagId, enabled }: { flagId: string; enabled: boolean }) => {
      const { data } = await api.patch<Flag>(
        `/projects/${projectId}/environments/${environmentId}/flags/${flagId}/enabled`,
        null,
        { params: { enabled } }
      );
      return data;
    },
    // Optimistic update: flag toggles should feel instant, with rollback on failure.
    onMutate: async ({ flagId, enabled }) => {
      await queryClient.cancelQueries({ queryKey: ["flags", projectId, environmentId] });
      const previous = queryClient.getQueryData<Page<Flag>>(["flags", projectId, environmentId, ""]);

      queryClient.setQueryData<Page<Flag>>(["flags", projectId, environmentId, ""], (old) =>
        old
          ? {
              ...old,
              content: old.content.map((flag) => (flag.id === flagId ? { ...flag, enabled } : flag)),
            }
          : old
      );

      return { previous };
    },
    onError: (_err, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["flags", projectId, environmentId, ""], context.previous);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["flags", projectId, environmentId] });
    },
  });
}

export function subscribeToFlagStream(
  environmentId: string,
  onChange: (flagKey: string) => void
): () => void {
  const source = new EventSource(`/api/v1/stream?environmentId=${environmentId}`);

  source.addEventListener("flag-updated", (event) => {
    const payload = JSON.parse((event as MessageEvent).data);
    onChange(payload.flagKey);
  });

  source.onerror = () => {
    console.warn("SSE connection interrupted, browser will auto-retry");
  };

  return () => source.close();
}
