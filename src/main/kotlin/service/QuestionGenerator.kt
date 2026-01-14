package org.example.service

import org.example.model.CriteriaConfig
import org.example.model.Category
import org.example.model.Criterion

/**
 * Generates questions for founders based on evaluation criteria
 */
object QuestionGenerator {
    
    /**
     * Generates questions from criteria configuration
     */
    fun generateQuestionsFromCriteria(criteriaConfig: CriteriaConfig?): List<Question> {
        if (criteriaConfig == null) return emptyList()
        
        val questions = mutableListOf<Question>()
        
        criteriaConfig.categories.forEach { category ->
            category.criteria.forEach { criterion ->
                val question = generateQuestionForCriterion(category, criterion)
                questions.add(question)
            }
        }
        
        return questions
    }
    
    /**
     * Generates a specific question for a criterion based on its description
     */
    private fun generateQuestionForCriterion(category: Category, criterion: Criterion): Question {
        val questionText = when {
            // Team & Governance questions
            criterion.name.contains("Industry Experience", ignoreCase = true) ||
            criterion.name.contains("Technical Team Experience", ignoreCase = true) -> {
                "Describe your core team's relevant industry experience. Include years of experience, previous companies, and track record in ${category.name.lowercase()}."
            }
            criterion.name.contains("Equity Structure", ignoreCase = true) -> {
                "Describe your equity structure. Include founder ownership percentages, investor equity, and any concerns about dilution or proxy holding disputes."
            }
            criterion.name.contains("Governance Structure", ignoreCase = true) -> {
                "Describe your governance mechanisms. Include board composition, risk control processes, compliance departments, and oversight structures."
            }
            
            // Business & Market questions
            criterion.name.contains("Customer Validation", ignoreCase = true) -> {
                when {
                    category.name.contains("Energy", ignoreCase = true) -> {
                        "Describe your customer validation. Include paying customers, Power Purchase Agreements (PPA), repurchase rates, and customer feedback."
                    }
                    category.name.contains("Tech", ignoreCase = true) -> {
                        "Describe your customer validation. Include paying customers (SaaS MRR/ARR), repurchase rates, customer unit price, and retention metrics."
                    }
                    else -> {
                        "Describe your customer validation. Include paying customers, repurchase rates, customer unit price, and retention metrics."
                    }
                }
            }
            criterion.name.contains("Market Opportunity", ignoreCase = true) -> {
                "Describe your market opportunity. Include market size, growth rate, target market dynamics, and policy support for your sector."
            }
            criterion.name.contains("Number of Patents", ignoreCase = true) -> {
                "Describe your intellectual property portfolio. Include number of patents filed, pending, or granted, and their relevance to your product."
            }
            criterion.name.contains("Competitive Barriers", ignoreCase = true) -> {
                "Describe your competitive advantages. Include technology differentiation, exclusive resources, first-mover advantages, and core capabilities vs competitors."
            }
            
            // Product & Technology questions
            criterion.name.contains("Innovation Level", ignoreCase = true) -> {
                "Describe the innovation level of your product/technology. Explain what makes it unique and how it provides a competitive advantage."
            }
            criterion.name.contains("Technical Feasibility", ignoreCase = true) -> {
                when {
                    category.name.contains("Energy", ignoreCase = true) -> {
                        "Describe the technical feasibility of your energy/cleantech solution. Include infrastructure requirements, implementation challenges, and technical validation."
                    }
                    category.name.contains("Tech", ignoreCase = true) -> {
                        "Describe the technical feasibility of your software/hardware solution. Include development capability, technical architecture, and implementation status."
                    }
                    else -> {
                        "Describe the technical feasibility of your solution. Include development status, technical validation, and implementation challenges."
                    }
                }
            }
            criterion.name.contains("Scalability", ignoreCase = true) -> {
                when {
                    category.name.contains("Energy", ignoreCase = true) -> {
                        "Describe how your energy solution can scale. Include infrastructure scalability, production capacity, and expansion plans."
                    }
                    category.name.contains("Tech", ignoreCase = true) -> {
                        "Describe how your technology solution can scale. Include cloud infrastructure, architecture scalability, and scaling plans."
                    }
                    else -> {
                        "Describe how your solution can scale. Include scalability plans, infrastructure requirements, and growth capacity."
                    }
                }
            }
            
            // Financial & Cash Flow questions
            criterion.name.contains("Cash Flow Health", ignoreCase = true) -> {
                "Describe your cash flow situation. Include operating cash flow status (positive/negative), cash runway (months of operations), and cash reserves."
            }
            criterion.name.contains("Cost Control", ignoreCase = true) -> {
                when {
                    category.name.contains("Energy", ignoreCase = true) -> {
                        "Describe your cost control capabilities. Include Levelized Cost of Energy (LCOE), operational efficiency, and cost management strategies."
                    }
                    category.name.contains("Tech", ignoreCase = true) -> {
                        "Describe your cost control metrics. Include Customer Acquisition Cost (CAC), Lifetime Value (LTV), and LTV/CAC ratio (ideally > 3 for SaaS)."
                    }
                    else -> {
                        "Describe your cost control. Include Customer Acquisition Cost (CAC), Lifetime Value (LTV), and cost management strategies."
                    }
                }
            }
            criterion.name.contains("Financing History", ignoreCase = true) -> {
                when {
                    category.name.contains("Energy", ignoreCase = true) -> {
                        "Describe your financing history. Include previous investors' background, financing timeliness, government grants/subsidies access, and funding rounds."
                    }
                    category.name.contains("Tech", ignoreCase = true) -> {
                        "Describe your financing history. Include previous investors' background, financing timeliness, presence of earnout clauses, and funding rounds."
                    }
                    else -> {
                        "Describe your financing history. Include previous investors' background, financing timeliness, and funding rounds."
                    }
                }
            }
            
            // Risk & Compliance questions
            criterion.name.contains("Policy Considerations", ignoreCase = true) -> {
                "Describe alignment with government policies. Include subsidies, incentives, regulatory support, and policy considerations relevant to your industry."
            }
            criterion.name.contains("Regulatory/Compliance Risk", ignoreCase = true) -> {
                "Describe your regulatory compliance status. Include permits, environmental regulations, energy policy considerations, and compliance measures."
            }
            criterion.name.contains("Privacy/Security Risk", ignoreCase = true) -> {
                "Describe your data privacy and cybersecurity measures. Include GDPR/CCPA compliance, security protocols, and data protection strategies."
            }
            criterion.name.contains("Policy Compliance Risk", ignoreCase = true) -> {
                "Describe your policy compliance. Include data security compliance, industry regulatory policy alignment, and compliance measures."
            }
            criterion.name.contains("Environmental Impact", ignoreCase = true) -> {
                "Describe your environmental impact. Include environmental impact assessment, sustainability measures, and carbon footprint reduction strategies."
            }
            criterion.name.contains("Legal Risk", ignoreCase = true) -> {
                when {
                    category.name.contains("Energy", ignoreCase = true) -> {
                        "Describe legal risks and mitigation. Include intellectual property disputes, land use rights, project contracts, and labor compliance."
                    }
                    category.name.contains("Tech", ignoreCase = true) -> {
                        "Describe legal risks and mitigation. Include intellectual property disputes, labor compliance, and open-source licensing issues."
                    }
                    else -> {
                        "Describe legal risks and mitigation. Include intellectual property disputes, labor compliance, and any legal concerns."
                    }
                }
            }
            criterion.name.contains("Collateral Guarantee", ignoreCase = true) -> {
                "Describe collateral and guarantees. Include founder joint guarantees, core technology pledge possibilities, and asset-backed security options."
            }
            
            // Default question based on description
            else -> {
                "Please provide information about: ${criterion.description}"
            }
        }
        
        return Question(
            id = "${category.name}_${criterion.name}",
            category = category.name,
            criterion = criterion.name,
            questionText = questionText,
            description = criterion.description,
            weight = criterion.weight
        )
    }
}

/**
 * Represents a question generated from criteria
 */
data class Question(
    val id: String,
    val category: String,
    val criterion: String,
    val questionText: String,
    val description: String,
    val weight: Double
)
