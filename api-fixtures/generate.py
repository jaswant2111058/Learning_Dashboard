import json

COURSES = [
    (1, "Python Programming", "John Smith", 13, [
        "Introduction", "Variables & Data Types", "Operators & Expressions", "Strings in Depth",
        "Lists, Tuples & Sets", "Dictionaries", "Conditionals", "Loops & Iteration",
        "Functions", "Scope & Closures", "Modules & Packages", "File I/O",
        "Exception Handling", "Object Oriented Programming", "Inheritance & Polymorphism",
        "Decorators", "Generators & Iterators", "Virtual Environments & pip",
        "Testing with pytest", "Capstone: CLI Task Manager",
    ]),
    (2, "Generative AI", "Sarah Williams", 6, [
        "What is Generative AI", "Tokens & Embeddings", "Transformer Architecture",
        "Prompt Engineering Basics", "Few-shot & Chain of Thought", "Structured Output",
        "Retrieval Augmented Generation", "Vector Databases", "Chunking Strategies",
        "Function & Tool Calling", "Agentic Workflows", "Evaluating LLM Output",
        "Guardrails & Safety", "Fine-tuning vs RAG", "Cost & Latency Optimisation",
        "Capstone: Build a RAG Assistant",
    ]),
    (3, "Full Stack Development", "David Brown", 7, [
        "Course Overview", "HTML Semantics", "CSS Layout & Flexbox", "Responsive Design",
        "JavaScript Fundamentals", "DOM & Events", "Async JavaScript & Fetch",
        "TypeScript Essentials", "React Components", "React Hooks", "State Management",
        "Client Side Routing", "Forms & Validation", "Node.js & npm", "Express Routing",
        "REST API Design", "Middleware & Error Handling", "SQL Basics",
        "Postgres & Relationships", "ORMs and Migrations", "Authentication with JWT",
        "Authorisation & Roles", "File Uploads", "Caching with Redis", "Testing the Stack",
        "Docker Basics", "CI/CD Pipelines", "Capstone: Deploy Full Stack App",
    ]),
    (4, "Data Structures & Algorithms", "Priya Nair", 0, [
        "Big-O Notation", "Arrays & Dynamic Arrays", "Two Pointers", "Sliding Window",
        "Linked Lists", "Stacks", "Queues & Deques", "Hash Tables", "Binary Search",
        "Sorting Algorithms", "Recursion & Backtracking", "Binary Trees", "Tree Traversals",
        "Binary Search Trees", "Heaps & Priority Queues", "Tries", "Graph Representations",
        "BFS & DFS", "Topological Sort", "Dijkstra's Algorithm", "Union Find",
        "Dynamic Programming 1D", "Dynamic Programming 2D", "Interview Patterns Review",
    ]),
    (5, "Cloud & DevOps Essentials", "Michael Chen", 18, [
        "Cloud Service Models", "Regions & Availability Zones", "Virtual Machines",
        "Object Storage", "Virtual Networks & Subnets", "Load Balancers",
        "Identity & Access Management", "Secrets Management", "Infrastructure as Code",
        "Terraform Fundamentals", "Containers & Docker", "Container Registries",
        "Kubernetes Objects", "Deployments & Rollouts", "Observability & Logging",
        "Metrics, Alerts & SLOs", "Cost Management", "Incident Response",
    ]),
]

def main():
    out = {"courses": []}
    for cid, title, instructor, completed, lessons in COURSES:
        total = len(lessons)
        progress = round(completed * 100 / total) if total else 0
        out["courses"].append({
            "id": cid,
            "title": title,
            "instructor": instructor,
            "progress": progress,
            "lessons": total,
            "syllabus": [
                {"id": cid * 100 + i + 1, "title": name, "completed": i < completed}
                for i, name in enumerate(lessons)
            ],
        })
        assert completed <= total
    print(json.dumps(out, indent=2))

main()
