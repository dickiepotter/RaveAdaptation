# RaveAdaptation — Resource Support System (RSSystem)

> A semantic-web / ontology-driven prototype from postgraduate (PhD) research,
> aimed at giving the **RAVE** visualization platform an automatic way to
> describe and allocate human–computer interaction (HCI) resources — so that a
> visualization could discover the user's input devices and bind its on-screen
> controls to whatever hardware was available, from a plain mouse to a data
> glove or tracked wand.
>
> **RAVE** — the **R**esource-**A**ware **V**isualization **E**nvironment,
> developed at **Cardiff University** (Dr. Ian Grimstead and colleagues) — is
> now an obsolete system. This repository preserves the *adaptation* that was
> prototyped on top of it, hence the repository name **RaveAdaptation** (the
> original Eclipse project and working folder used the spelling *RaveAdaption*). The
> Eclipse project itself is named **RSSystem** (the "Resource Support System").

## Provenance

This work was a **collaboration between two universities**, documented in the
project's final report and conference paper held in the academic archive:

- **Richard Potter** — Department of Computer Science, **University of Hull**
  (the ontological resource-description research; the author of this code).
- **Dr. Ian Grimstead** — Cardiff School of Computer Science, **Cardiff
  University** (author of RAVE).

It was funded by a **CompuSteer / DTI–EPSRC travel bursary** (CompuSteer
*#QF005*) covering visits between Hull and Cardiff during 2006. The ontology
work formed a **case study within Richard Potter's PhD thesis** (the broader
thesis was carried out at Hull), while RAVE itself was the Cardiff system being
extended. Primary sources:

- I. Grimstead and R. Potter, *"Reacting to HCI Devices: Initial Work Using
  Resource Ontologies with RAVE"*, **First CompuSteer workshop, September
  2006** (Hull) — paper, slides and final report archived under
  `Post Graduate Work/Projects/RAVE/`.

---

## 1. What this activity was about

RAVE was already **resource-aware** about *compute, rendering and network*
resources — it could pick alternative servers, dataset resolutions, compression
schemes and so on. But for **user input it only understood a mouse or stylus**:
the lowest common denominator chosen to keep RAVE portable across platforms. It
had no concept of richer devices (dataglove, tracked wand, SpaceBall) that offer
more degrees of freedom and a more natural, immersive interaction.

The research captured here closes that gap with an **ontological approach to
resource detection, description, selection and inclusion**, built around a
*"requirement request → provision"* metaphor:

1. A visualization **widget announces what kind of interaction it needs** — for
   example "three degrees of rotary freedom" — rather than naming a device.
2. The **ontology is queried for the best-fit input resource**. If nothing
   matches, the request can be relaxed (e.g. drop the "rotary" restriction) and
   retried, so the application negotiates down to whatever is actually present.
3. The chosen device is **automatically mapped to drive the widget** (the user
   may override), feeding live device state back into the visualization.

The key advantage over contemporary systems such as **DirectX Input** is that
the application works in terms of *requirements* ("I need this kind of input")
instead of *enumerating fixed device types* ("Mouse", "Joystick", …). The same
abstraction also lets RAVE simplify its UI when a capable device is present —
e.g. the separate "Fly", "Drive", "Move" and "Rotate" modes become redundant
with a full six-DoF device.

The work has two halves:

1. **A knowledge model** — an OWL ontology (`res/Resource.owl`) that formally
   describes visualization/HCI resources: the physical devices, the sensory
   experiences they can produce, the user interactions they can capture, the
   software interfaces they expose, and the data types that flow across those
   interfaces.
2. **A software skeleton** — this Java framework (`RSSys`) that loads that
   ontology with the Jena semantic-web toolkit and provides the runtime
   plumbing (widget registration, interaction requirements, resource mapping)
   needed to match interaction widgets to available resources.

This sits alongside the related PhD outputs found in the academic archive —
the GRC'05 poster *"Describing Visualization Resources"*, the Eurovis'06
submission, the DSV-IS *"Resource Management"* paper, and the
*"An Ontological Approach to Visualization Resources"* paper — all of which
develop the same theme.

---

## 2. The ontology (`res/Resource.owl`)

Authored in **Protégé** (the file still carries Protégé/`owl-ontologies.com`
namespaces) and consumed at runtime via Jena. It is the heart of the activity:
the Java code is mostly scaffolding around it.

As described in the project slides, a resource is described using **six
inter-referenced taxonomies** — *Hardware*, *User actions*, *Sensory
experiences*, *Software interfaces*, *Software interface items* and
*Variables* — which is exactly the structure of `Resource.owl`. The ontology
organises the world of a visualization resource into these top-level,
mutually-disjoint hierarchies:

| Branch | Purpose | Example sub-classes |
| --- | --- | --- |
| `Hardware_Resource` | Physical HCI devices | `Mouse`, `Keyboard`, `Monitor`, `Data_Glove`, `Motion_Tracking_System` |
| `Sensory_Experience` | What the user can perceive (output side) | `Visual_Sense`, `Auditory_Sense`, `Tactile_Sense`, `Vestibular_Sense`, `Proprioception_Sense`, … |
| `User_Interaction` | What the user can do (input side) | `Movement_Interaction`, `Extremity_Interaction`, `Vocal_Interaction`, `Gesture`, `Vocal_Command` |
| `Software_Interface` / `Software_Interface_Item` | The programmatic surface of a device | `Method_Interface`, `Field_Interface`, `Property_Interface` |
| `Variable` / `Value` | The data types carried across interfaces | `Numeric_data` (`Integer`, `Short`, `Floating_Point`), `Non-Numeric_data` (`Character`, `String`, `Object`, `Array`), `Enumeration`, `Conditional_Variable` |

These are linked by object/datatype properties that let the model express *how*
a resource behaves, for example:

- `capturesInteractions` — a hardware resource captures certain `User_Interaction`s
- `providesExperience` — a hardware resource produces certain `Sensory_Experience`s
- `hasInterfaces` / `hasInterfaceItems` — a device exposes software interfaces made of methods, fields and properties
- `hasParameter` / `hasReturnValue` / `providesAccessTo` — the data flowing in and out of those interface items
- `representsInteraction` / `representsExperience` — ties a concrete data value back to the interaction or sensation it stands for

A worked instance is included: the **5DT Data Glove 16** is modelled down to
individual SDK calls (e.g. `fdSetSensorRaw`), their parameters, and the raw
sensor values that map onto specific finger movements — demonstrating that a
real device's API can be expressed entirely in the ontology.

Supporting standard ontologies/vocabularies used during development live in
`res/etc/` (OWL, RDF-Schema, DAML+OIL, Dublin Core, Jena policy files, etc.).

---

## 3. The Java framework (`RSSys`)

A deliberately thin, partly-stubbed prototype — it establishes the *structure*
of the resource-support system rather than a finished implementation. Package
`RSSys` contains:

| Class / Interface | Role |
| --- | --- |
| `Resource` | Represents an available resource; loads its description from `Resource.owl` via Jena (`OntModel` + `OntDocumentManager`, using `res/etc/ont-policy.rdf` and an alt-entry mapping to the local OWL file). `getAvailableResources()` is the intended enumeration point. |
| `AWidget` | Abstract base for interaction widgets. Each widget registers itself with the `WidgetManager` and must declare its needs via `getInteractionReq()`. Holds a reference to the `IResourceInterface` it ends up bound to. |
| `InteractionReq` | Describes what a widget *requires* of a resource (the requirement object that would be matched against the ontology). Placeholder in this prototype. |
| `WidgetManager` | **Singleton** registry of all widgets in a visualization. `provideHCIResources()` is the entry point that would drive resource discovery and allocation. |
| `ResourceMapper` | Where the matching/"choosing algorithm" belongs — maps the set of registered widgets onto the set of available resources. Stubbed, marked as the core algorithm to be developed. |
| `IResourceInterface` | The handle a widget uses once it has been bound to a concrete resource. Marker interface in this prototype. |

### Demonstration harness (`TestImp`)

A minimal example showing the intended usage pattern:

- `TestVisualization` — `main()` entry point. Gets the `WidgetManager`
  singleton, constructs some widgets, and calls `provideHCIResources()`.
- `TestWidgetA`, `TestWidgetB` — trivial concrete `AWidget`s that return an
  (empty) `InteractionReq`.

### Intended runtime flow

```
TestVisualization.Init()
        │  creates widgets
        ▼
AWidget(constructor) ──register──► WidgetManager (singleton)
        │
        ▼
WidgetManager.provideHCIResources()
        │  gather each widget's InteractionReq
        ▼
ResourceMapper.Map(widgets)
        │  Resource.getAvailableResources()  ← described by Resource.owl (via Jena)
        │  run the choosing algorithm: requirements ⟷ resource capabilities
        ▼
bind each widget to an IResourceInterface
```

In other words: widgets say *what kind of interaction they need*, resources
declare *what they can do* (in the ontology), and the mapper reasons over both
to wire them together.

### How the code maps to the published mechanism

The report describes the same machinery on the RAVE side, which this framework
generalises:

| Concept in the paper | Class here |
| --- | --- |
| `IWidget` — a widget the ontology support can interrogate for its requirements (degrees of freedom, etc.) | `AWidget` + `InteractionReq` |
| `WidgetManager` registering all widgets and feeding control values to the right widget | `WidgetManager` |
| Ontology returns a **Java class that feeds live device state into the widget** | `IResourceInterface` |
| Best-fit / heuristic selection of an input resource for a requirement | `ResourceMapper` |

---

## 4. Proof of concept (what was actually demonstrated)

Because of Java/Java3D's limited support for input peripherals, the only device
beyond a standard mouse that could be wired up was a **wheel mouse (3 DoF)** —
supporting a fuller device would have required writing a native device driver.
That was deemed enough for a proof of concept.

A **1-D slider widget** was built that announced its interaction requirement to
the ontology and received an appropriate input resource in return. The slider
was used to **steer the temperature of a molecular-dynamics simulation of a
liquid** — the "steering" half of *computational steering* — with its value
reflected over the network to all users collaboratively viewing the dataset.

This demonstration was presented at the **First CompuSteer workshop in Hull,
September 2006**.

Reported limitations of the approach at the time included the heavy supporting
infrastructure required by the ontology tooling (originally a full database to
host the ontology, though an ontology *file* could be interrogated instead) and
the immaturity of the publicly available semantic-web tools.

---

## 5. Status

This is **research prototype / proof-of-concept code**, not a finished product:

- The ontology is well developed (including a fully modelled real device).
- The Java layer is scaffolding — several methods (`ResourceMapper.Map`,
  `Resource.getAvailableResources`, `WidgetManager.provideHCIResources`,
  `InteractionReq`) are intentionally empty or stubbed, marked `/* todo */`
  and `/* INCOMPLETE */`.
- Only sources are tracked; compiled `.class` build artefacts are removed and
  `.gitignore`d (recompile from source — see *Building & running*). The original
  Eclipse `.project` / `.classpath` are kept for historical authenticity.

It is preserved for historical and reference value: it documents how an
ontology-driven resource-description and allocation layer was prototyped for
the (now obsolete) Cardiff **RAVE** visualization environment.

---

## 6. Repository layout

```
RaveAdaptation/
├── .project / .classpath        Eclipse project metadata (project name: "RSSystem")
├── classdiagram.dnx             UML class diagram of the framework
├── RSSys/                       The Resource Support System framework (sources + compiled classes)
│   ├── Resource.java            Resource described by the ontology (loaded via Jena)
│   ├── AWidget.java             Abstract interaction widget
│   ├── InteractionReq.java      A widget's interaction requirement
│   ├── WidgetManager.java       Singleton widget registry / allocation entry point
│   ├── ResourceMapper.java      Widget→resource matching algorithm (stub)
│   └── IResourceInterface.java  Handle to a bound resource
├── TestImp/                     Minimal demonstration harness
│   ├── TestVisualization.java   main() — wires up the example
│   ├── TestWidgetA.java
│   └── TestWidgetB.java
├── res/
│   ├── Resource.owl             The visualization-resource ontology (Protégé/OWL)
│   └── etc/                     Supporting standard ontologies & Jena config
└── lib/                         Jena 2.x and dependencies (ARQ, IRI, ICU4J, Xerces, log4j, …)
```

---

## 7. Building & running

The project targets an early Eclipse JDT workspace and the bundled **Jena 2.x**
libraries in `lib/` (the Jena classes are dated 2006).

- **Classpath:** every JAR in `lib/` plus the compiled output directory.
- **Working directory:** the project root, because `Resource.java` resolves the
  ontology with the relative paths `file:res/etc/ont-policy.rdf` and
  `file:./res/Resource.owl`.
- **Entry point:** `TestImp.TestVisualization`.

Compile and run the demo (needs a full **JDK**, run from the project root):

```bash
# compile all sources to ./bin
javac -cp "lib/*" -d bin RSSys/*.java TestImp/*.java

# run the demo harness
java -cp "bin;lib/*" TestImp.TestVisualization     # ';' classpath separator on Windows; use ':' on Linux/macOS
```

Expected console output from the demo harness is simply:

```
Visualization has been initialised
```

(The shipped `main()` never invokes the ontology path — that interesting
behaviour was never wired up; see *Status* above. To exercise it you instantiate
`RSSys.Resource` directly, which runs its Jena ontology load.)

### Verified to still run on a modern JVM (June 2026)

Despite its age, the prototype runs **unmodified on a current JDK** — the
2005–2007 code and the 2006-vintage Jena 2.x jars are compatible with Java 17:

| What was run | Result on Temurin **JDK 17** |
| --- | --- |
| Demo harness compiled and run (`javac` then `java TestImp.TestVisualization`) — the original bundled bytecode was even **Java 1.2**, major version 46 | ✅ prints `Visualization has been initialised` |
| Parsing `res/Resource.owl` through Jena (`jena.rdfcat -out N-TRIPLE`) | ✅ **1,623 triples**, no errors |
| The real project code: `new RSSys.Resource(...)` → `retrieveDescription()` (OWL `OntModel` + `OntDocumentManager` + `ont-policy.rdf` + alt-entry) | ✅ loads cleanly — **1,842-statement OWL model, 47 named classes** |

> **One gotcha — you need a *complete* JRE/JDK, not a trimmed one.** Jena uses
> `java.rmi.server.UID`, so a cut-down `jlink` runtime that omits the `java.rmi`
> module fails with `NoClassDefFoundError: java/rmi/server/UID`. A standard
> Temurin/Oracle/OpenJDK distribution includes `java.rmi` and works fine.
>
> Note also that to *recompile or modify* the Java you need a full **JDK**
> (with `javac`); a JRE alone will only run the existing `.class` files.

If the goal is simply to *explore the ontology* on a modern stack, see the
companion **[ResourceOntology](https://github.com/dickiepotter/ResourceOntology)**
project — a .NET + Svelte/Cytoscape web visualiser of essentially the same
`Resource.owl`.
