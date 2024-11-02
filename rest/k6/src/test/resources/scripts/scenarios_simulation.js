import http from 'k6/http';

export const options = {
  scenarios: {
    shared_iter_scenario: {
      executor: 'shared-iterations',
      vus: 2,
      iterations: 10,
      startTime: '0s',
    },
    per_vu_scenario: {
      executor: 'per-vu-iterations',
      vus: 3,
      iterations: 3,
      startTime: '5s',
    },
  },
};

export default function () {
  http.get('https://test.k6.io/');
}