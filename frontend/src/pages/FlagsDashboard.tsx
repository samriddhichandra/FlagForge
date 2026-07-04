import { useEffect, useState } from "react";
import { subscribeToFlagStream, useFlags, useToggleFlag } from "../api/useFlags";

interface Props {
  projectId: string;
  environmentId: string;
}

export default function FlagsDashboard({ projectId, environmentId }: Props) {
  const [search, setSearch] = useState("");
  const { data, isLoading, isError, refetch } = useFlags(projectId, environmentId, search);
  const toggle = useToggleFlag(projectId, environmentId);

  useEffect(() => {
    const unsubscribe = subscribeToFlagStream(environmentId, () => refetch());
    return unsubscribe;
  }, [environmentId, refetch]);

  if (isLoading) return <div className="p-6 text-slate-400">Loading flags...</div>;

  return (
    <div className="p-6 space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-xl font-semibold text-slate-100">Flags</h1>
        <input
          className="w-full rounded-md border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-100 outline-none focus:border-emerald-400 sm:w-72"
          placeholder="Search flags..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>

      <div className="overflow-hidden rounded-lg border border-slate-800 divide-y divide-slate-800">
        {isError ? (
          <div className="px-4 py-8 text-sm text-rose-300">Unable to load flags.</div>
        ) : null}

        {!isError && data?.content.length === 0 ? (
          <div className="px-4 py-8 text-sm text-slate-400">No flags found.</div>
        ) : null}

        {data?.content.map((flag) => (
          <div key={flag.id} className="flex items-center justify-between gap-4 px-4 py-3">
            <div className="min-w-0">
              <p className="truncate text-sm font-medium text-slate-100">{flag.name}</p>
              <p className="truncate font-mono text-xs text-slate-500">{flag.key}</p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={flag.enabled}
              aria-label={`${flag.enabled ? "Disable" : "Enable"} ${flag.name}`}
              disabled={toggle.isPending}
              onClick={() => toggle.mutate({ flagId: flag.id, enabled: !flag.enabled })}
              className={`h-6 w-11 shrink-0 rounded-full p-0.5 transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
                flag.enabled ? "bg-emerald-500" : "bg-slate-700"
              }`}
            >
              <span
                className={`block h-5 w-5 rounded-full bg-white transition-transform ${
                  flag.enabled ? "translate-x-5" : "translate-x-0"
                }`}
              />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
