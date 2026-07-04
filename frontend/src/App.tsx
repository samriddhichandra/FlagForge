import FlagsDashboard from "./pages/FlagsDashboard";

const demoProjectId = "00000000-0000-0000-0000-000000000001";
const demoEnvironmentId = "00000000-0000-0000-0000-000000000002";

export default function App() {
  return (
    <main className="min-h-screen bg-slate-950 text-slate-100">
      <FlagsDashboard projectId={demoProjectId} environmentId={demoEnvironmentId} />
    </main>
  );
}
