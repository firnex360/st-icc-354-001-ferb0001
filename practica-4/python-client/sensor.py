"""
Cliente IoT simulado que genera datos aleatorios de temperatura y humedad
y los publica en el topic 'notificacion_sensores' de ActiveMQ vía STOMP.
"""

import stomp
import json
import random
import time
import os
from datetime import datetime


# Configuración desde variables de entorno
BROKER_HOST = os.environ.get('BROKER_HOST', 'localhost')
BROKER_PORT = int(os.environ.get('BROKER_PORT', '61613'))
DEVICE_ID = int(os.environ.get('DEVICE_ID', '1'))
INTERVAL_SECONDS = int(os.environ.get('INTERVAL_SECONDS', '60'))
TOPIC = '/topic/notificacion_sensores'


class SensorConnectionListener(stomp.ConnectionListener):
    """Listener para eventos de conexión STOMP."""

    def on_error(self, frame):
        print(f'[Sensor #{DEVICE_ID}] ERROR: {frame.body}')

    def on_disconnected(self):
        print(f'[Sensor #{DEVICE_ID}] Desconectado del broker')

    def on_connected(self, frame):
        print(f'[Sensor #{DEVICE_ID}] Conectado al broker ActiveMQ')


def generate_sensor_data():
    """Genera un payload JSON con datos aleatorios del sensor."""
    return {
        'fechaGeneracion': datetime.now().strftime('%d/%m/%Y %H:%M:%S'),
        'IdDispositivo': DEVICE_ID,
        'temperatura': round(random.uniform(15.0, 40.0), 2),
        'humedad': round(random.uniform(30.0, 90.0), 2)
    }


def main():
    """Bucle principal: conecta al broker y envía datos periódicamente."""
    print(f'[Sensor #{DEVICE_ID}] Iniciando cliente IoT...')
    print(f'[Sensor #{DEVICE_ID}] Broker: {BROKER_HOST}:{BROKER_PORT}')
    print(f'[Sensor #{DEVICE_ID}] Topic: {TOPIC}')
    print(f'[Sensor #{DEVICE_ID}] Intervalo: {INTERVAL_SECONDS}s')

    while True:
        conn = None
        try:
            # Crear conexión STOMP
            conn = stomp.Connection([(BROKER_HOST, BROKER_PORT)])
            conn.set_listener('sensor_listener', SensorConnectionListener())
            conn.connect('admin', 'admin', wait=True)

            print(f'[Sensor #{DEVICE_ID}] Conexión establecida. Enviando datos cada {INTERVAL_SECONDS}s...')

            while conn.is_connected():
                # Generar y enviar datos
                data = generate_sensor_data()
                payload = json.dumps(data)

                conn.send(
                    destination=TOPIC,
                    body=payload,
                    content_type='application/json'
                )

                print(f'[Sensor #{DEVICE_ID}] Enviado: {payload}')

                # Esperar el intervalo configurado
                time.sleep(INTERVAL_SECONDS)

        except Exception as e:
            print(f'[Sensor #{DEVICE_ID}] Error de conexión: {e}')
            print(f'[Sensor #{DEVICE_ID}] Reintentando en 5 segundos...')
            time.sleep(5)
        finally:
            if conn and conn.is_connected():
                conn.disconnect()


if __name__ == '__main__':
    main()
