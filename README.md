# 🧠 BugMind Core

BugMind Core is the foundational Java library for intelligent log analysis, designed to transform unstructured application logs into structured, queryable data.  
It serves as the core logic engine for future BugMind modules such as:
- 🧩 **BugMind AI** — automated anomaly classification  
- 📊 **BugMind Dashboard** — visual log analytics  
- 🤖 **BugMind Insight** — LLM-based code and runtime intelligence  

---

## 🌟 Vision

Modern applications produce massive, unstructured log data.  
BugMind Core enables **structure, meaning, and intelligence** in this data by:
- Parsing raw text logs into structured records  
- Normalizing patterns (timestamps, levels, exceptions)  
- Preparing machine-readable features for downstream AI/ML systems  

Ultimately, BugMind aims to be the **AI layer for developer observability** — connecting code behavior with intelligent diagnostics.

---

## ⚙️ Tech Stack

| Component | Details |
|------------|----------|
| **Language** | Java 17 |
| **Build Tool** | Apache Maven |
| **Testing** | JUnit 5 |
| **Code Style** | Google Java Format |
| **Supported IDEs** | Eclipse, IntelliJ IDEA, VS Code |

---
---

### 🧩 Log Retrieval API

| Method | Endpoint | Description |
|---------|-----------|-------------|
| GET | `/api/logs/level/{level}` | Retrieve all parsed logs matching the given level (INFO, WARN, ERROR, etc.) |



