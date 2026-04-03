job "abyss-social-api" {
  datacenters = ["dc1"]
  type        = "service"
  priority    = 50

  group "abyss-social" {
    count = 1

    network {
      mode = "bridge"
      port "http" {
        static = 8080
        to     = 8080
      }
      port "mongodb" {
        static = 27017
        to     = 27017
      }
    }

    service {
      name     = "abyss-social-api"
      port     = "http"
      provider = "nomad"
      tags     = ["spring", "api", "social"]

      check {
        type     = "http"
        path     = "/actuator/health"
        interval = "30s"
        timeout  = "5s"
      }

      check {
        type     = "tcp"
        interval = "10s"
        timeout  = "2s"
      }
    }

    service {
      name     = "mongodb"
      port     = "mongodb"
      provider = "nomad"
      tags     = ["database", "mongodb"]

      check {
        type     = "tcp"
        interval = "10s"
        timeout  = "2s"
      }
    }

    task "mongodb" {
      driver = "podman"

      lifecycle {
        hook    = "prestart"
        sidecar = true
      }

      config {
        image      = "mongo:latest"
        force_pull = false
        ports      = ["mongodb"]
      }

      env {
        MONGO_INITDB_DATABASE = "abyss-social"
      }

      resources {
        cpu    = 500
        memory = 1024
      }

      restart {
        attempts = 2
        interval = "5m"
        delay    = "10s"
        mode     = "delay"
      }

      logs {
        max_files     = 2
        max_file_size = 10
      }
    }

    task "abyss-social-api" {
      driver = "podman"

      config {
        image      = "localhost/abyss-social-api:1.0"
        force_pull = false
        ports      = ["http"]
      }

      env {
        SPRING_DATA_MONGODB_URI  = "mongodb://localhost:27017/abyss-social"
        SERVER_PORT              = "8080"
        SPRING_PROFILES_ACTIVE   = "production"
        JAVA_TOOL_OPTIONS        = "-Xmx512m -Xms256m"
      }

      resources {
        cpu    = 500
        memory = 512
      }

      restart {
        attempts = 3
        interval = "5m"
        delay    = "25s"
        mode     = "delay"
      }

      logs {
        max_files     = 2
        max_file_size = 10
      }
    }
  }
}