# BeezKit implementation architecture

- Start from the host call site and keep its default path to one expression where possible.
- Require a root host only for UI requiring a lifecycle-owned overlay; make that installation explicit.
- Keep interfaces and models independent of optional third-party implementations.
- Avoid umbrella artifacts until dependency choices are stable.
- Preserve binary-compatible public surfaces once a module reaches Stable.
- Use `internal` for implementation coordinators, stores, nodes, collectors, and adapters.
- Treat the module specification as canonical; README remains an inventory and entry point.

