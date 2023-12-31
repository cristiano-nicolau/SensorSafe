import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import {useEffect, useRef} from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Toastify from './Toastify';



function Copyright(props) {
  return (
    <Typography variant="body2" color="text.secondary" align="center" {...props}>
      {'Copyright © '}
      <Link color="inherit" href="https://mui.com/">
        Your Website
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
}

// TODO remove, this demo shouldn't need to reset the theme.

const defaultTheme = createTheme();

export default function SignIn() {
  const emailRef = useRef(null);
  const passwordRef = useRef(null);
  const adminCodeRef = useRef(null); 
  const [redirect, setRedirect] = useState(false);
  const navigate = useNavigate();


  const handleSubmit = async (event) => {
    event.preventDefault();

    const formData = new FormData(event.currentTarget);

    try {
      const response = await fetch('http://localhost:8080/sensorsafe/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: formData.get('username'),
          password: formData.get('password'),
          // caso o admin code nao seja colocado no form, o valor default é 0
          adminCode: formData.get('adminCode') ? formData.get('adminCode') : 0
         
        }),
      });
  
      if (!response.ok) {
        Toastify.error('Login failed');
        throw new Error('Network response was not ok');
      }
  
      const data = await response.json();
  
      // Check the response structure and adjust accordingly
      if (data && data.message === 'Login successful') {
        // Registration was successful
        Toastify.success('Login successful');
        sessionStorage.setItem('Token:', data.token);
        navigate('/reports');
        } else {
        Toastify.error('Login failed, try again. Error: ' + data.message);
        

        // Perform additional actions if needed
      }
    } catch (error) {
        Toastify.info('Error connecting to server');
      // Perform additional error handling if needed
    }
  };


  return (
    <ThemeProvider theme={defaultTheme}>
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
            <LockOutlinedIcon />
          </Avatar>
          <Typography component="h1" variant="h5">
            Sign in
          </Typography>
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="username"
              name="username"
              autoComplete="off"
              autoFocus
              inputRef={emailRef}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              id="password"
              label="Password"
              name="password"
              type="password"
              autoComplete="off"
              autoFocus
              inputRef={passwordRef}
            />
            <TextField
              margin="normal"
              fullWidth
              id="adminCode"
              label="Admin Code (optional) - ask your admin for this code"
              name="adminCode"
              autoComplete="off"
              autoFocus
                
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
            >
              Sign In
            </Button>
            <Grid container>
              <Grid item>
                <Link href="/signup" variant="body2">
                  {"Don't have an account? Sign Up"}
                </Link>
              </Grid>
            </Grid>
          </Box>
        </Box>
        <Copyright sx={{ mt: 8, mb: 4 }} />
      </Container>
    </ThemeProvider>
  );
}