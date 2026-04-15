Here's a rough timeline assuming solo development with part-time hours:

**Phase 1 — Foundation (2-3 weeks)**
- Set up MCP-919 workspace - done
- Build basic module system (enable/disable, keybinds, config I/O) - done
- Get JSON config reading/writing working
- Basic event bus (render, tick, key press) - done
- Start QML launcher skeleton (just launches the jar for now) - starting later

**Phase 2 — HUD Modules (2-3 weeks)**
- Keystrokes HUD - done
- CPS display - done
- FPS display - done
- Armor durability HUD
- Potion effect timers
- Food saturation display
- Custom crosshair
- Direction compass
- Kill/death tracker

**Phase 3 — Utility Modules (1-2 weeks)**
- Toggle sprint - done
- No click delay - done
- Zoom - built in with optifine
- Freelook
- Auto gg
- Chat filter/highlights
- Inventory organizer
- FOV locker - built in with optifine

**Phase 4 — Performance (2 weeks)**
- Entity culling
- Particle limiter
- GC allocation reduction
- FPS uncap in menus
- Frame limiter replacement

**Phase 5 — Combat/Neutral Modules (3-4 weeks)**
- Optimal Aim Indicator - done
- Opponent reach visualizer - done
- Enemy health display - done
- Trade detector - done
- Strafe suggestion
- Rod throw predictor
- Momentum block suggester
- Remaining combat HUD modules

**Phase 6 — Launcher (2-3 weeks)**
- Full QML UI
- Microsoft auth flow
- Module toggle UI
- Update system
- Packaging

**Phase 7 — Polish + Testing (2 weeks)**
- Chroma/RGB support
- GUI theme override
- Hit color customizer
- Bug fixing, server compatibility testing

**Total: ~16-20 weeks**

Start with Phase 1 when you're ready?
