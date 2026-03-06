FROM python:3.12-slim

WORKDIR /app

# Instalar dependencias Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar script del sensor
COPY sensor.py .

# Ejecutar con output sin buffer para ver logs en Docker
CMD ["python", "-u", "sensor.py"]
