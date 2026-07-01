import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ErrorAlert } from './ErrorAlert';

describe('ErrorAlert', () => {
  it('renders nothing when there is no message', () => {
    const { container } = render(<ErrorAlert message={null} />);
    expect(container).toBeEmptyDOMElement();
  });

  it('renders the message in an alert role when provided', () => {
    render(<ErrorAlert message="Something went wrong" />);
    expect(screen.getByRole('alert')).toHaveTextContent('Something went wrong');
  });
});
