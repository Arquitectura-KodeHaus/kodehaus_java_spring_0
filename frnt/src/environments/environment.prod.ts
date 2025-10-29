export const environment = {
  production: true,
  apiUrl: (window as any)['env']?.['apiUrl'] || 'https://backend-service-java-2-729022607150.us-central1.run.app/api'
};
