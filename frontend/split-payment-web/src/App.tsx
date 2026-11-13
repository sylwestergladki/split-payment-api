
import { Button } from "@/components/ui/button";

function App() {
  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-slate-950 px-6 py-16">
      {/* Dekoracyjne tło */}
      <div className="pointer-events-none absolute -top-32 -left-32 h-96 w-96 rounded-full bg-teal-500/20 blur-3xl" />
      <div className="pointer-events-none absolute -right-32 -bottom-32 h-96 w-96 rounded-full bg-violet-500/20 blur-3xl" />

      <div className="relative w-full max-w-2xl rounded-3xl border border-white/10 bg-slate-900/80 p-8 text-center shadow-2xl backdrop-blur-xl sm:p-12">
        <div className="mb-8 inline-flex items-center justify-center gap-2 rounded-full border border-teal-400/20 bg-teal-400/10 px-4 py-2 text-sm font-medium text-teal-300">
          <span className="h-2 w-2 rounded-full bg-teal-400" />
          Split Payment
        </div>

        <h1 className="mb-5 text-4xl font-bold tracking-tight text-white sm:text-5xl">
          Wspólne wydatki.
          <span className="block bg-gradient-to-r from-teal-300 to-violet-400 bg-clip-text text-transparent">
            Proste rozliczenia.
          </span>
        </h1>

        <p className="mx-auto mb-10 max-w-lg text-lg leading-relaxed text-slate-400">
          Zarządzaj grupami, dziel rachunki i rozliczaj się ze znajomymi.
          Bez arkuszy, bez liczenia, bez chaosu.
        </p>

        <div className="flex flex-wrap items-center justify-center gap-4">
          <Button className="h-12 rounded-xl bg-teal-400 px-6 font-semibold text-slate-950 hover:bg-teal-300">
            Moje grupy
          </Button>

          <Button
            variant="outline"
            className="h-12 rounded-xl border-white/20 bg-white/5 px-6 text-white hover:bg-white/10 hover:text-white"
          >
            Dodaj wydatek
          </Button>
        </div>

        <div className="mt-12 grid grid-cols-3 gap-3 border-t border-white/10 pt-8 text-center">
          <div>
            <p className="text-xl font-bold text-teal-300">01</p>
            <p className="mt-1 text-xs text-slate-400">Utwórz grupę</p>
          </div>
          <div>
            <p className="text-xl font-bold text-violet-300">02</p>
            <p className="mt-1 text-xs text-slate-400">Dodaj wydatki</p>
          </div>
          <div>
            <p className="text-xl font-bold text-teal-300">03</p>
            <p className="mt-1 text-xs text-slate-400">Rozlicz saldo</p>
          </div>
        </div>
      </div>
    </main>
  );
}

export default App;
