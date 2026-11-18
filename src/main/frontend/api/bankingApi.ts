import type {
  AccountDto,
  ApiErrorResponse,
  CreateAccountRequest,
  CreateCustomerRequest,
  CreateTransactionRequest,
  CustomerDto,
  ProductDto,
  TransactionDto,
  UpdateAccountStatusRequest,
  UpdateCustomerRequest,
} from '../types/banking';

export class ApiError extends Error {
  public readonly status: number;
  public readonly error?: string;

  constructor(message: string, status: number, error?: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.error = error;
  }
}

async function requestJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: {
      'Accept': 'application/json',
      ...(init?.body ? { 'Content-Type': 'application/json' } : {}),
      ...init?.headers,
    },
    ...init,
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get('content-type') ?? '';
  const isJson = contentType.includes('application/json');
  const payload = isJson ? await response.json().catch(() => null) : null;

  if (!response.ok) {
    const errBody = payload as ApiErrorResponse | null;
    const message =
      errBody?.message ||
      errBody?.error ||
      response.statusText ||
      `Request failed with status ${response.status}`;
    throw new ApiError(message, response.status, errBody?.error);
  }

  return payload as T;
}

export const bankingApi = {
  getProducts(): Promise<ProductDto[]> {
    return requestJson<ProductDto[]>('/api/v1/products');
  },

  getCustomers(): Promise<CustomerDto[]> {
    return requestJson<CustomerDto[]>('/api/v1/customers');
  },

  searchCustomers(name: string): Promise<CustomerDto[]> {
    const query = encodeURIComponent(name.trim());
    return requestJson<CustomerDto[]>(`/api/v1/customers/search?name=${query}`);
  },

  getCustomer(idOrName: string | number): Promise<CustomerDto> {
    return requestJson<CustomerDto>(`/api/v1/customers/${encodeURIComponent(String(idOrName).trim())}`);
  },

  createCustomer(request: CreateCustomerRequest): Promise<CustomerDto> {
    return requestJson<CustomerDto>('/api/v1/customers', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },

  updateCustomer(id: number, request: UpdateCustomerRequest): Promise<CustomerDto> {
    return requestJson<CustomerDto>(`/api/v1/customers/${id}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    });
  },

  deleteCustomer(id: number): Promise<void> {
    return requestJson<void>(`/api/v1/customers/${id}`, {
      method: 'DELETE',
    });
  },

  getAccounts(customerId?: number): Promise<AccountDto[]> {
    const suffix = customerId !== undefined ? `?customerId=${customerId}` : '';
    return requestJson<AccountDto[]>(`/api/v1/accounts${suffix}`);
  },

  getAccount(id: string | number): Promise<AccountDto> {
    return requestJson<AccountDto>(`/api/v1/accounts/${encodeURIComponent(String(id).trim())}`);
  },

  createAccount(request: CreateAccountRequest): Promise<AccountDto> {
    return requestJson<AccountDto>('/api/v1/accounts', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },

  updateAccountStatus(id: number, request: UpdateAccountStatusRequest): Promise<AccountDto> {
    return requestJson<AccountDto>(`/api/v1/accounts/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify(request),
    });
  },

  getTransactionsByAccount(accountId: string | number): Promise<TransactionDto[]> {
    return requestJson<TransactionDto[]>(
      `/api/v1/transactions/${encodeURIComponent(String(accountId).trim())}`,
    );
  },

  createTransaction(request: CreateTransactionRequest): Promise<TransactionDto> {
    return requestJson<TransactionDto>('/api/v1/transactions', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },
};
