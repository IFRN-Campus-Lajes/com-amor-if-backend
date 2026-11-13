package com.amorif.entities;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "execucao_automatica")
@Getter
@Setter
@NoArgsConstructor
public class ExecucaoAutomatica {
    @Id
    private UUID id;
    @Column(nullable = false, length = 24)
    private String origem;
    @Column(nullable = false, length = 24)
    private String status;
    @Column(nullable = false, length = 255)
    private String solicitante;
    @Column(name = "ano_letivo_id")
    private Long anoLetivoId;
    private Integer ano;
    private Integer bimestre;
    @Column(nullable = false)
    private Instant inicio;
    private Instant fim;
    @Column(nullable = false)
    private int criados;
    @Column(nullable = false)
    private int existentes;
    @Column(nullable = false)
    private int inelegiveis;
    @Column(length = 1000)
    private String erro;
}
