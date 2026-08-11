# Compose API guidelines

- Prefer a non-composable Modifier factory and `Modifier.Node` for stateful, frequently recomposed behavior.
- Keep state local to each Modifier or explicitly hoisted; never key behavior only by a display tag.
- Update reusable nodes rather than replacing them when parameters change.
- Provide semantics for clicks, loading states, messages, actions, and tooltips.
- Respect reduced-motion settings and avoid independent infinite animations in large lists.
- Own overlays through a composable host and release pending callbacks and jobs when it leaves composition.
- Test recomposition, parameter changes, detach/reattach, configuration changes, and multiple owners.

